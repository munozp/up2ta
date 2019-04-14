/*********************************************************************
 * File: proyecto.h
 * Description: envio y recepcion de datos por tuberias 
 *
 * Contiene las inclusiones, macros y prototipos de funciones que se necesitan para
 * el manejo de las tuberias que se han implementado para comunicar los procesos.
 *
 * Author: Hector Franco 2012
 *
 *********************************************************************/ 

#ifndef _PROYECTO_H
#define _PROYECTO_H

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

#define MENSAJE_HEURISTICA '0'
#define MENSAJE_CAMINO '1'
#define MENSAJE_SALIR '2'
#define MENSAJE_COSTE '3'

#define PRECISION(n,m) "%0" #n "." #m "f"	/*Numero de decimales(n) que se envian por las tuberias*/

#define EPSILON 0.0000001			/*Precision en la comparacion de double*/


#define NOMBRE_TUBERIA_FFAstar "/tmp/tuberia-ff-astar"
#define NOMBRE_TUBERIA_AstarFF "/tmp/tuberia-astar-ff"
#define T_PUNTO 10

int abrirTuberiaLectura (const char *nombre, mode_t modo, int flags);
int abrirTuberiaEscritura (const char *nombre, mode_t modo, int flags);
int cerrarTuberia(int descr);
size_t enviarTuberia(int fd, const void *buf, size_t count);
size_t recibirTuberia(int fd, void *buf, size_t count);

int comparador_igual(double a, double b);
int comparador_menor(double a, double b);
int comparador_mayor(double a, double b);
int comparador_menor_igual(double a, double b);
int comparador_mayor_igual(double a, double b);
double absoluto(double arg);
void my_perror(char *m);

#endif /* _PROYECTO_H */
