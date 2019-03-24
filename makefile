#!/bin/sh
#
# Makefile for Aplicacion v1.0
#


####### DIRECTORIOS
INC	= inc
SRC	= src
OBJ	= obj
BIN	= bin

FF	= FF

####### FLAGS
COO     = g++
CC	= gcc

####### FLAGS FF

TYPE	= 
ADDONS	= 

CFLAGS	= -O6 -Wall -g -ansi $(TYPE) $(ADDONS)
# -g -pg

LIBS    = -lm 


##################### FF ###################

PDDL_PARSER_SRC	= $(SRC)/$(FF)/scan-fct_pddl.tab.c \
	$(SRC)/$(FF)/scan-ops_pddl.tab.c \
	#$(SRC)/$(FF)/scan-probname.tab.c \
	$(SRC)/$(FF)/lex.fct_pddl.c \
	$(SRC)/$(FF)/lex.ops_pddl.c 

PDDL_PARSER_OBJ = $(OBJ)/$(FF)/scan-fct_pddl.tab.o \
	$(OBJ)/$(FF)/scan-ops_pddl.tab.o 


FUENTESF 	= $(SRC)/$(FF)/main.c \
	$(SRC)/$(FF)/memory.c \
	$(SRC)/$(FF)/output.c \
	$(SRC)/$(FF)/parse.c \
	$(SRC)/$(FF)/inst_pre.c \
	$(SRC)/$(FF)/inst_easy.c \
	$(SRC)/$(FF)/inst_hard.c \
	$(SRC)/$(FF)/inst_final.c \
	$(SRC)/$(FF)/orderings.c \
	$(SRC)/$(FF)/relax.c \
	$(SRC)/$(FF)/search.c \
	$(SRC)/$(BUSQUEDA)/proyecto.c \
	$(SRC)/$(FF)/lista.c \
	$(SRC)/$(FF)/hash.c

OBJETOSF 	= $(OBJ)/$(FF)/main.o \
	$(OBJ)/$(FF)/memory.o \
	$(OBJ)/$(FF)/output.o \
	$(OBJ)/$(FF)/parse.o \
	$(OBJ)/$(FF)/inst_pre.o \
	$(OBJ)/$(FF)/inst_easy.o \
	$(OBJ)/$(FF)/inst_hard.o \
	$(OBJ)/$(FF)/inst_final.o \
	$(OBJ)/$(FF)/orderings.o \
	$(OBJ)/$(FF)/relax.o \
	$(OBJ)/$(FF)/search.o \
	$(OBJ)/$(FF)/proyecto.o \
	$(OBJ)/$(FF)/lista.o \
	$(OBJ)/$(FF)/hash.o


####### Build rules


aplicacion: $(BIN)/ff

$(BIN)/ff: $(OBJETOSF) $(PDDL_PARSER_OBJ) 
	$(CC) -o $(BIN)/ff $(OBJETOSF) $(PDDL_PARSER_OBJ) $(CFLAGS) $(LIBS)

$(OBJETOSF): $(FUENTESF) $(PDDL_PARSER_SRC)
	$(CC) -I $(INC)/$(FF) -I $(INC)/$(BUSQUEDA) -c $(FUENTESF) $(PDDL_PARSER_SRC)
	mv *.o $(OBJ)/$(FF)

# pddl syntax
$(SRC)/$(FF)/scan-fct_pddl.tab.c: $(SRC)/$(FF)/scan-fct_pddl.y $(SRC)/$(FF)/lex.fct_pddl.c
	bison -pfct_pddl -bscan-fct_pddl $(SRC)/$(FF)/scan-fct_pddl.y
	mv scan-fct_pddl.tab.c $(SRC)/$(FF)

$(SRC)/$(FF)/scan-ops_pddl.tab.c: $(SRC)/$(FF)/scan-ops_pddl.y $(SRC)/$(FF)/lex.ops_pddl.c
	bison -pops_pddl -bscan-ops_pddl $(SRC)/$(FF)/scan-ops_pddl.y
	mv scan-ops_pddl.tab.c $(SRC)/$(FF)

$(SRC)/$(FF)/lex.fct_pddl.c: $(SRC)/$(FF)/lex-fct_pddl.l
	flex -Pfct_pddl $(SRC)/$(FF)/lex-fct_pddl.l
	mv lex.fct_pddl.c $(SRC)/$(FF)

$(SRC)/$(FF)/lex.ops_pddl.c: $(SRC)/$(FF)/lex-ops_pddl.l
	flex -Pops_pddl $(SRC)/$(FF)/lex-ops_pddl.l
	mv lex.ops_pddl.c $(SRC)/$(FF)


clean: 
	@rm $(BIN)/ff
	@rm -f *.o *.bak *~ *% core *_pure_p9_c0_400.o.warnings \
        \#*\# $(RES_PARSER_SRC) $(PDDL_PARSER_SRC)

veryclean:
	@rm $(BIN)/ff $(OBJ)/$(FF)/*.o
	@rm -f ff H* J* K* L* O* graph.* *.symbex gmon.out \
	$(PDDL_PARSER_SRC) \
	lex.fct_pddl.c lex.ops_pddl.c lex.probname.c \
	*.output

depend:
	makedepend -- $(FUENTESF) $(PDDL_PARSER_SRC)

lint:
	lclint -booltype Bool $(FUENTESF) 2> output.lint
