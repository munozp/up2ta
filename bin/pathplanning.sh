#!/bin/bash
if [ $# -eq 3 ] 
then
	java -jar PathPlanning.jar $1 $2 $3
else
	java -jar PathPlanning.jar $1 $2 $3 $4
fi
exit
