#ifndef HASH_H
#define	HASH_H

typedef
	struct hash_e
	{
		char *celda;
		unsigned int *index;
		struct hash_e *next;
	}
t_hash_e;

typedef
	struct hash
	{
		t_hash_e *first;
		t_hash_e *last;
	}
t_hash;

void inicializarHASH(t_hash *hash);
void addHASH(t_hash *lista, t_hash_e *elemento);
int getIndexHASH(t_hash *lista, char *id, unsigned int dim);
char *getCeldaHASH(t_hash *lista, unsigned int index);
void liberarHASH(t_hash *lista);

t_hash_e *nuevoHashE(char *celda, unsigned int index);

#endif /* HASH_H */
