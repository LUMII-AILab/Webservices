#!/bin/bash

while test 1=1
do
	./webservice.sh >log.txt 2>error_log.txt
	echo "Restarting.. `date`"
done

