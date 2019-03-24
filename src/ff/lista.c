#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "lista.h"
#include "hash.h"

//Funciones que se aplican sobre t_cache

void inicializar(t_cache *cache)
{
	cache->dimensiones = 0;
	cache->first = NULL;
	cache->last = NULL;
	inicializarHASH(&(cache->hash));
	return;
}

void add(t_cache *cache, char *celda)
{
	int index = getIndexHASH(&(cache->hash),celda,cache->dimensiones);
	if (index < 0)
	{
		t_id *id;
		id = nuevoID();
		addHASH(&(cache->hash),nuevoHashE(celda,cache->dimensiones));
		cache->dimensiones++;
		addID(cache, id);
		redimensionar(cache);
	}
	return;
}

void redimensionar(t_cache *cache)
{
	t_id *i;
	i = cache->first;
	while (i!=NULL)
	{
		t_valor *nValor;
		if ((i->first)==NULL)
		{
			int num;
			num = 0;
			while(num < cache->dimensiones)
			{
				nValor = (t_valor *) malloc(sizeof(t_valor));
				inicializarValor(nValor);
				addValor(i,nValor);
				num++;
			}
		}
		else
		{
			nValor = (t_valor *) malloc(sizeof(t_valor));
			inicializarValor(nValor);
			addValor(i,nValor);
		}
		i = i->next;
	}
}

void mostrar(t_cache *cache)
{
	printf("\n");
	t_id *i;
	int index;
	index = 0;
	i = cache->first;
	while (i != NULL)
	{
		printf("\t%s",getCeldaHASH(&(cache->hash), index));
		index++;
		i = i->next;
	}
	printf("\n");
	index = 0;
	i = cache->first;
	while (i != NULL){
		printf("%s",getCeldaHASH(&(cache->hash), index));
		index++;
		t_valor *j;
		j = i->first;
		while(j!=NULL)
		{
			if (j->elemento == NULL)
			{
				printf("\tNULL");
			}
			else
			{
				printf("\t%.2f", *(j->elemento));
			}
			j = j->next;
		}
		printf("\n");
		i = i->next;
	}
	return;
}

void setElemento(t_cache *cache, unsigned int id, unsigned int valor, t_elemento v)
{
	if ((cache->dimensiones >= id) && (cache->dimensiones >= valor)){
		t_id *i;
		i = getID(cache->first, id);
		t_valor *j;
		j = getValor(i->first,valor);
		if (j->elemento==NULL)
		{
			j->elemento = (t_elemento *) malloc(sizeof(t_elemento));
			*(j->elemento) = v;
		} else {
			*(j->elemento) = v;
		}
	} else {
		printf("Error: funcion setValor\n");
		exit(1);
	}
}

t_elemento *getElemento(t_cache *cache, int id, int valor)
{
	if ((cache->dimensiones >= id) && (cache->dimensiones >= valor)){
		t_id *i;
		i = getID(cache->first, id);
		t_valor *j;
		j = getValor(i->first,valor);
		return j->elemento;
	} else {
		printf("Error: funcion setValor -> fuera de dimensiones\n");
		return NULL;
	}
}

void liberar(t_cache *cache)
{
	liberarHASH(&(cache->hash));
	t_id *iID, *jID;
	t_valor *iVALOR, *jVALOR;
	iID = cache->first;
	if (iID != NULL)
	{
		do
		{
			iVALOR = iID->first;
			do
			{
				jVALOR = iVALOR->next;
				if (iVALOR->elemento != NULL)
				{
					free(iVALOR->elemento);
				}
				free(iVALOR);
				iVALOR=jVALOR;
			} while (iVALOR != NULL);
			jID = iID->next;
			free(iID);
			iID=jID;
		} while (iID != NULL);
	}
	cache->first = NULL;
	return;
}

//Funciones que se aplican sobre t_id

t_id *nuevoID()
{
	t_id *nuevo_id;
	nuevo_id = (t_id *) malloc(sizeof(t_id));
	nuevo_id->next = NULL;
	nuevo_id->first = NULL;
	nuevo_id->last = NULL;
	return nuevo_id;
}

void addID(t_cache *cache, t_id *id)
{
	if (cache->first == NULL)
	{
		cache->first = id;
		cache->last = id;
	} else {
		cache->last->next = id;
		cache->last = id;
	}
	return;
}

t_id *getID(t_id *id, unsigned int index){
	t_id *i;
	i = id;
	while ((i!=NULL) && (index != 0)){
		i = i->next;
		index--;
	}
	if (i!=NULL){
		return i;
	} else {
		return NULL;
	}
}

//Funciones que se aplican sobre t_valor

void inicializarValor(t_valor *v)
{
	v->next = NULL;
	v->elemento = NULL;
	return;
}
void addValor(t_id *id, t_valor *v)
{
	if (id->first == NULL)
	{
		id->first = v;
		id->last = v;
	} else {
		id->last->next = v;
		id->last = v;
	}
	return;
}

t_valor *getValor(t_valor *valor, unsigned int index)
{
	t_valor *i;
	i = valor;
	while((i!=NULL) && (index != 0)){
		i = i->next;
		index--;
	}
	if (i!=NULL) {
		return i;
	} else {
		return NULL;
	}
}
