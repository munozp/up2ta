#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "hash.h"

void inicializarHASH(t_hash *hash)
{
	hash->last = NULL;
	hash->first = NULL;
	return;
}

void addHASH(t_hash *lista, t_hash_e *elemento)
{
	if (lista->first == NULL) {
		lista->first = elemento;
		lista->last = elemento;
	} else {
		if((strcmp(lista->first->celda,elemento->celda))>0)
		{
			/*
			 * El nuevo elemento es menor que el primer elemento que existia -> el nuevo elemento pasa a ser el primero.
			 */
			elemento->next = lista->first;
			lista->first = elemento;
		}
		else
		{
			t_hash_e *anterior,*comparar;
			anterior = lista->first;
			comparar = anterior->next;
			while (comparar!=NULL)
			{
				if((strcmp(comparar->celda,elemento->celda))>0)
				{
					break;
				}
				anterior = comparar;
				comparar = anterior->next;
			}
			if(comparar==NULL)
			{
				anterior->next = elemento;
				elemento->next = NULL;
				lista->last = elemento;
			}
			else
			{
				elemento->next = comparar;
				anterior->next = elemento;
			}
		}
	}
	return;
}

int getIndexHASH(t_hash *lista, char *id, unsigned int dim)
{
	t_hash_e *e;
	e = lista->first;
	while(e!=NULL)
	{
		if(strcmp(id,e->celda)==0)
		{
			break;
		}
		e = e->next;
	}
	if(e==NULL)
	{
		return -1;
	}
	else
	{
		return *(e->index);
	}
}

char *getCeldaHASH(t_hash *lista, unsigned int index)
{
	t_hash_e *e;
	e = lista->first;
	while(e!=NULL)
	{
		if(*(e->index) == index)
		{
			break;
		}
		e = e->next;
	}
	if(e==NULL)
	{
		return NULL;
	}
	else
	{
		return e->celda;
	}
}

void liberarHASH(t_hash *lista)
{
	if ((lista->first != NULL) && (lista->last != NULL))
	{
		t_hash_e *i;
		i = lista->first;
		while(i!=lista->last)
		{
			lista->first = i->next;
			if(i->celda != NULL)
			{
				free(i->celda);
			}
			if(i->index != NULL)
			{
				free(i->index);
			}
			free(i);
			i = lista->first;
		}
		free(i);
	}
	return;
}

t_hash_e *nuevoHashE(char *celda, unsigned int index)
{
	t_hash_e *nuevo;
	nuevo = (t_hash_e *) malloc (sizeof(t_hash_e));
	nuevo->next = NULL;
	nuevo->celda = (char *) malloc (sizeof(*celda));
	strcpy(nuevo->celda,celda);
	nuevo->index = (int *) malloc (sizeof(int));
	*(nuevo->index) = index;
	return nuevo;
}
