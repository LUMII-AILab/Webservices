#!/bin/bash

cd $(dirname $0)
java -mx3g -cp target/webservices-1.0.4-SNAPSHOT-jar-with-dependencies.jar lv.semti.morphology.pipetool.WordPipe  $*

