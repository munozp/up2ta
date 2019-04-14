#ifndef LISTA_H
#define	LISTA_H

#define TIPO double
#define FORMATO_TIPO " %f"

#include "hash.h"

typedef TIPO t_elemento;

typedef 
	struct valor
	{
		t_elemento *elemento;
		struct valor *next;
	} 
t_valor;

typedef 
	struct id
	{
		struct id *next;
		t_valor *first;
		t_valor *last;
	}
t_id;

typedef
	struct cache
	{
		t_hash hash;
		unsigned int dimensiones;
		t_id *first;
		t_id *last;
	}
t_cache;

void inicializar(t_cache *cache);
void add(t_cache *cache, char *celda);
void mostrar(t_cache *cache);
void redimensionar(t_cache *cache);
void setElemento(t_cache *cache, unsigned int id, unsigned int valor, t_elemento v);
t_elemento *getElemento(t_cache *cache, int id, int valor);
void liberar(t_cache *cache);

t_id *nuevoID();
void addID(t_cache *cache, t_id *id);
t_id *getID(t_id *id, unsigned int index);

void inicializarValor(t_valor *v);
void addValor(t_id *id, t_valor *v);
t_valor *getValor(t_valor *valor, unsigned int index);

#endif /* LISTA_H */
