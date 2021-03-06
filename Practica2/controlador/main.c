/* Entrada 5 argc
-La clave cifrada
-Direccion del Diccionario
-Numero de procesos reventador
-Direccion del reventador
-Direccion del fichero de salida
*/
#define DEBUG
#ifdef DEBUG
#define DEBUG_MSG(...) printf(__VA_ARGS__)
#else
#define DEBUG_MSG(...)
#endif
#define LOCK_TEST_TIMEOUT 5
#define LOCK_RETRY_TRY 1
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <signal.h>
#include <unistd.h>
#include <string.h>
struct Config{
  char f_claves[256]; //Contains dictionary file
  char MD5key[33];  //Clave cifrada
  char f_reventador[256];
  int proc_reventador;  //Numero de procesos reventadores
  char exit_file[256];  //Fichero de salida
};
struct Child_data{
  pid_t pid;
  int state;
};
struct Config config;
struct Child_data *child;
char *lock_name;
void insert_clave(char *clave, char *fichero_salida);
long file_length(char *file);
long ret_next_line(char *file, long pos, long dict_size);
void end_program(int state);
int LoadConfig(int argc, char *argv[], struct Config *config);
int lock_program(char *file, int timeout, int max_retry);
static void signal_handler(int sig);
int main(int argc, char * argv[]){
  if(lock_program(argv[0], LOCK_TEST_TIMEOUT, LOCK_RETRY_TRY)){
    fflush(stdout); //Ya sabemos que el fichero esta ahi. No necesitamos esa info.
    perror("Program already being executed\n");
    exit(1);
  }
  LoadConfig(argc, &argv[0], &config);
  child=calloc(config.proc_reventador, sizeof(struct Child_data));
  long dict_size=file_length(config.f_claves);
  long dict_div=dict_size/config.proc_reventador; //
  long posiciones[2]={0, ret_next_line(config.f_claves, dict_div, dict_size)};
  char pos_0[20];
  char pos_1[20];

  //Creamos mascara;
  //sigset_t mascara=enmascarar(int SIGCHLD);
  DEBUG_MSG("El tamaño del diccionario es %ld y el primer segmento termina en %ld \n", dict_size, dict_div);
  if ((signal(SIGCHLD, signal_handler)==SIG_ERR)|(signal(SIGINT, signal_handler)==SIG_ERR)|(signal(SIGTERM, signal_handler)==SIG_ERR)){
    perror("[main] An error ocurred while setting the signal handler.\n");
    exit(-1);
  }
  sigset_t mascara;
  sigemptyset(&mascara);
  sigaddset(&mascara, SIG_ERR);
  sigaddset(&mascara, SIGINT);
  sigaddset(&mascara, SIGTERM); //Creamos mascara para deshabilitar interrupciones en segmentos criticos.
  
  DEBUG_MSG("[Controlador][Main] Iniciamos la creacion de Hijos\n");
  pid_t pid_hijo;
  insert_clave(config.MD5key, config.exit_file);
  for(int i=0; i< config.proc_reventador; i++){
    sigprocmask(SIG_BLOCK, &mascara, NULL); //Bloqueamos interrupciones
    switch (child[i].pid = fork()) {
      case (pid_t)-1:
        //perror("Error en la creacion de hijo num %d", i);
        fprintf(stderr, "Error en la creacion de hijo num: %d", i);
        perror("");
        end_program(1);
      case (pid_t) 0:
        sprintf(pos_0,"%ld", posiciones[0]);
        sprintf(pos_1,"%ld", posiciones[1]);
        execl(config.f_reventador, "reventador", config.f_claves, pos_0, pos_1, config.MD5key, config.exit_file, NULL);
        perror("Execl no ha creado un nuevo hijo.\n");
        end_program(1);  //Deberia notificar al proceso padre para decirle que no estoy haciendo lo que deberia...
      default:
        child[i].state=1; //Set to on;
        sigprocmask(SIG_UNBLOCK, &mascara, NULL); //Desbloqueamos interrupciones
        posiciones[0]=posiciones[1];
        if((dict_div*(i+3))>=dict_size){posiciones[1]=dict_size;}
        else posiciones[1]=ret_next_line(config.f_claves, dict_div*(i+2), dict_size);
        
    }
  }
  //All kidos have been created. Now wait until they end
  //Data has been saved into child struct
  do{
    sleep(60);
  }while(1);
}

void insert_clave(char *clave, char *fichero_salida){
  FILE *fich;
  if((fich = fopen(fichero_salida, "w"))==NULL){
    perror("[Controlador] Error. Fichero de salida no encontrado!");
    end_program(1);//exit(1);
  }
  fprintf(fich,"%s",clave);
  fclose(fich);
}
long file_length(char *file){
  FILE *fich;
  if((fich=fopen(file, "r"))==NULL){
    perror("[Controlador][file_length] Error. Diccionario no encontrado\n");
    exit(1);
  }
  fseek(fich, 0, SEEK_END);
  long length = ftell(fich);
  fclose(fich);
  return length;
}
long ret_next_line(char *file, long pos, long dict_size){
  FILE *fich;
  if((fich=fopen(file, "r"))==NULL){
    perror("[Controlador][ret_next_line] Error. Diccionario no encontrado\n");
    exit(1);
  }
  fseek(fich, pos, SEEK_SET);
  while(fgetc(fich)!='\n' & ftell(fich) <= (dict_size-1));//Stay here until you find next \n
  pos=ftell(fich);
  fclose(fich);
  return pos;
}
void end_program(int state){
  //Kill all children and kill ourselves Avada Kedavra
  switch (state) {
    case 0:
      DEBUG_MSG("Exito\n");
      for(int i=0; i<config.proc_reventador; i++){
        if(child[i].state!=0){
          if(kill(child[i].pid, SIGTERM)==-1){
            fprintf(stderr, "Error haciendo avada kedavra de hijo num: %d", i);
            perror("");
          }
        }
      }
      remove(lock_name);
    default:
      remove(lock_name);
      exit(1);
  }
  exit(1);
}
int lock_program(char *file, int timeout, int max_retry){
  lock_name=calloc((strlen(file)+5), sizeof(char));
  memcpy(lock_name, file, strlen(file));
  char lock[]=".lock";
  strncat(lock_name, lock, 6);
  DEBUG_MSG("El fichero de bloqueo es: %s\n", lock_name);
  FILE *fich;
  for(int retry=0; retry <max_retry; retry++){
    if((fich=fopen(lock_name, "wx"))==NULL){
      DEBUG_MSG("Retry number %d\n", (retry+1));
      sleep(timeout);
    }
    else{
      return 0;
    }
  }
  return 1;
}
static void signal_handler(int sig){
  switch(sig){
    case SIGINT:
      DEBUG_MSG("Se ha producido un SIGINT\n");
      end_program(1);
      break;
    case SIGTERM:
      DEBUG_MSG("Se ha producido un SIGTERM, cerramos todo\n");
      end_program(1);
      break;
    case SIGCHLD:
      DEBUG_MSG("Un hijo a producido una llamada al sistema\n");
      pid_t pid_hijo;
      int e;
      do{
        pid_hijo=wait3(&e, WNOHANG, NULL);
        if(pid_hijo>(pid_t) 0 && (WIFEXITED(e) || WIFSIGNALED(e))){//WIFEXITED -> Terminacion normal WIFSIGNALED -> Terminacion por señal
          if(WIFEXITED(e)){//Terminacion normal
            int shutdown=0;
            for(int i=0; i<config.proc_reventador; i++){
              if(child[i].pid==pid_hijo){
                child[i].state=0; //off. Con esta funcion nos aseguramos de que el programa no intenta matar despues a un proceso que no esta vivo
              }
              shutdown = shutdown | child[i].state; //Luego haremos el ! de tal forma que si hay algun hijo vivo, esto no se apague, a no ser que haya habido un exito
            }
            switch(WEXITSTATUS(e)){ //Miramos salidas
              case 0:
                DEBUG_MSG("Success\n");
                end_program(0);
              case 1:
                DEBUG_MSG("Un hijo a terminado sin exito\n");
                if(!shutdown){//Si no hay ningun hijo vivo, apagamos todo y nos vamos
                  end_program(1);
                }
                break;	
              case -1:
                DEBUG_MSG("Se ha producido un error grave\n");
                end_program(1);
            }
          }
        }
      }while(pid_hijo > (pid_t) 0);
  }
}

int LoadConfig(int argc, char *argv[], struct Config *config){
  /*Esta funcion comprueba que todos los datos sean metidos
  , y los guarda en config
  */
  if(argc!=6){
    perror("[LoadConfig], Numero de argumentos esperados es 5!");
    exit(-1);
  }
  //Primer argumento es la clave cifrada
  if(!sscanf(argv[1], "%s", (config->MD5key))){
    perror("[LoadConfig], MD5key is not correctly set up");
    exit(-1);
  }
  //Segundo argumento es Direccion del Diccionario
  if(!sscanf(argv[2], "%s", (config->f_claves))){
    perror("[LoadConfig], Dictionary is not correctly set up");
    exit(-1);
  }
  //El tercer argumento es Numero de procesos reventador
  if(!sscanf(argv[3], "%d", &(config->proc_reventador))){
    perror("[LoadConfig], int fin_dict is not correctlyset up");
    exit(-1);
  }
  //El cuarto argumento es la direccion del reventador
  if(!sscanf(argv[4], "%s", config->f_reventador)){
    perror("[LoadConfig], MD5key is not correctly set up");
    exit(-1);
  }
  //Y el quinto argumento es el fichero de salida.
  if(!sscanf(argv[5], "%s", (config->exit_file))){
    perror("[LoadConfig], exit_file is not correctly set up");
    exit(-1);
  }
  return 0;

}
