#!/bin/bash

java -mx1200m -cp dist/webservices.jar:dist/morphology.jar:dist/CRF.jar:lib/org.restlet.jar:dist/transliterator.jar:dist/PhoneticTranscriber.jar:dist/lvsegmenter.jar:lib/json_simple-1.1.jar:lib/org.restlet.ext.json.jar:lib/org.json.jar:lib/mysql-connector-java-5.1.19-bin.jar:lib/commons-collections4-4.0.jar:lib/commons-lang3-3.4.jar lv.semti.morphology.webservice.MorphoServer $*

