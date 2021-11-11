
/*******************
argv[1] Clave cifrada
argv[2] Ruta del fichero diccionario
argv[3] Numero de reventadores
argv[4] Nombre del fichero que contiene diccionario
argv[5] Nombre del fichero donde se apunta el resultado
*/
#include <stdio.h>
#include <stdlib.h>
#define DEBUG
#ifdef DEBUG
#define DEBUG_MSG(...) printf(__VA_ARGS__)
#else
#define DEBUG_MSG(...)
#endif
/*param clave -Clave a añadir al fichero
Esta funcion intoduve la clave cifrada en el fichero correspondiente
*/
void insert_clave(char *clave);

/**
param -char fichero -Contiene la ruta del diccionario

esta funcion devuelve  cuantas palabras tiene un fichero**/
int contar_palabras(char *fichero);

/**
param int divisor -posicion del fichero
param char *fichero -contiene la ruta del diccionario
Devuelve en una variable tipo long la posicion del inicio de la siguiente
palabra encontrada a partir de la posicion dada**/
long dividir(int divisor, char *fichero);


int main(int argc, char * argv[]){
  int reventadores;     //Almacena el numero de reventadores a crear
  long octeto_ini = 0;  //Almacena el valor del octeto inicial del trozo
  long octeto_end;      //Almacena el valor del octeto final del trozo

  sscanf(argv[3],"%d", &reventadores);

  if(argc == 6){
    insert_clave(argv[1]);
    for(int i = 1; i < reventadores; i++){
      octeto_end = dividir(i * contar_palabras(argv[2])/reventadores, argv[2]);
      DEBUG_MSG("reventador words %ld %ld %s clave\n",octeto_ini, octeto_end, argv[1]);
      octeto_ini = octeto_end + 1;
    }
    DEBUG_MSG("reventador words %ld %d %s clave\n",octeto_ini, contar_palabras(argv[2]), argv[1]);
  }else{
    DEBUG_MSG("Argumentos mal introducidos\n");
  }
}

void insert_clave(char *clave){
  FILE *fich;
  fich = fopen("clave.txt", "w");
  if(fich){
    fDEBUG_MSG(fich,"%s",clave);
  }else{
    DEBUG_MSG("no su pudo abrir el cichero clave.txt");
  }
  fclose(fich);
}

int contar_palabras(char *fichero){
  int count = 0;
  char aux[80] ;
  FILE *fich;
  fich = fopen(fichero, "r");

  if(fich){
    while(fscanf(fich, "%c", aux) != EOF){
      count++;
    }
  }else{
    DEBUG_MSG("no su pudo abrir el diccionario");
  }
  return count;
}

long dividir(int divisor, char *fichero){
  FILE *fich;
  fich = fopen(fichero, "r");

  if(fich){
    fseek(fich, divisor, SEEK_SET);
    while(fgetc(fich) != '\n'){

    }

  }else{
    DEBUG_MSG("no su pudo abrir el diccionario");
  }
  return ftell(fich);
}
