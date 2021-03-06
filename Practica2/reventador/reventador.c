/*Reventador.c
Entrada: 5 arg
            - Nombre de fichero que contiene diccionario
            - Fila desde la que empezar a leer el fichero. (Octeto, segun la practica)
            - Fila en la que se tiene que detener el reventador
            - la clave cifrada
            - Nombre de fichero donde debe introducir el resultado con la clave.

Have fun!
*/
//#define DEBUG
#ifdef DEBUG
#define DEBUG_MSG(...) printf("PID: %d ", pid);printf(__VA_ARGS__)
#else
#define DEBUG_MSG(...)
#endif
#include "probar-claves.c"
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <string.h>
//#include "probar-claves.c"
struct Config{
  char f_claves[256];  //Contains the dictionary file
  int ini_dict;   //
  int fin_dict;
  char MD5key[33];
  char exit_file[256];
};

int pid=0;
int parentID=0;
int LoadConfig(int argc, char *argv[], struct Config *config);
uint8_t* hex_str_to_uint8(const char* string);
static void termination(int signum);


int main(int argc, char *argv[]){
  //printfMD5("pruebalinea11");
  pid= getpid();
  parentID= getppid();
  if (signal(SIGTERM, termination)==SIG_ERR){
    perror("[main] An error ocurred while setting the signal handler.\n");
    exit(-1);
  }
  DEBUG_MSG("Iniciando Reventador. PID is %d and Parent is %d \n", pid, parentID);
  struct Config config;
  FILE *Diccionario;
  LoadConfig(argc, &argv[0], &config);
  if((Diccionario=fopen(config.f_claves, "r"))==NULL){
    perror("[main] Couldn't open key dictionary");
    return -1;
  }
  //fseek(Diccionario, config.ini_dict, SEEK_SET); Sets pointer to a CHAR not to a line
  //char *line=NULL;
  char line[60];
  //size_t len=0, line_length=0;
  //int pos=0;
  //char *end_key;
  char end_key[60];
  //while((line_length=getline(&line, &len, Diccionario))!=-1 && pos<config.ini_dict-1)pos++; //Lets move the pointer to the starting pos
  fseek(Diccionario, config.ini_dict, SEEK_SET);
  //while((line_length=getline(&line, &len, Diccionario))!=-1 && pos<config.fin_dict-1){
  while(fgets(line, 60, Diccionario)!=NULL & ftell(Diccionario)<=config.fin_dict){
    DEBUG_MSG("Probando clave: %s", line);
    //if(probar_1palabra(line, config.MD5key)==0){
    strtok(line,"\n");
    //end_key=(char *)malloc(len);
    if(probar_combinaciones_palabra(line, end_key, config.MD5key)==0){
      FILE *ExitFile;
      if((ExitFile=fopen(config.exit_file, "a"))==NULL){
        perror("[main] Couldn't open exit file");
        exit(1);
      }
      //fseek(ExitFile, 0, SEEK_END);
      fputs("\n", ExitFile);
      fputs(end_key, ExitFile);
      DEBUG_MSG("[main] The key has been found!: %s \n", end_key);
      fclose(ExitFile);
      exit(0);
      return 0;//Success!
    }
    //pos++;
  }
  exit(1);  //File not found! :(
}
int LoadConfig(int argc, char *argv[], struct Config *config){
  /*Esta funcion comprueba que todos los datos sean metidos
  , y los guarda en config
  */
  if(argc!=6){
    perror("[LoadConfig], Numero de argumentos esperados es 5!");
    exit(-1);
  }
  if(!sscanf(argv[1], "%s", (config->f_claves))){
    perror("[LoadConfig], f_claves is not correctly set up");
    exit(-1);
  }
  if(!sscanf(argv[2], "%d", &(config->ini_dict))){
    perror("[LoadConfig], int ini_dict is not correctlyset up");
    exit(-1);
  }
  if(!sscanf(argv[3], "%d", &(config->fin_dict))){
    perror("[LoadConfig], int fin_dict is not correctlyset up");
    exit(-1);
  }
  //char hexaMD5key[32];
  if(!sscanf(argv[4], "%s", config->MD5key)){
    perror("[LoadConfig], MD5key is not correctly set up");
    exit(-1);
  }
  //convertir_hexadecimal_binario(hexaMD5key, config->MD5key);
  if(!sscanf(argv[5], "%s", (config->exit_file))){
    perror("[LoadConfig], exit_file is not correctly set up");
    exit(-1);
  }
  return 0;

}

static void termination(int signum){
  fflush(stdout); //Change! Signal Handler should do the bare minimum, set a flag or something, but not ffslush or allocate memory or anything like that
  DEBUG_MSG("Child Process %d needs to terminate", pid);
  switch(signum){
    case SIGINT:  //Program Interrupt Ctrl+c
      DEBUG_MSG("Se ha producido un SIGINT, omitimos\n");
      break;
    case SIGTERM:
      DEBUG_MSG("Se ha producido un SIGTERM, cerramos todo\n");
      exit(1);
      break;


  }
}
