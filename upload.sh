#!/bin/bash
JAR=$(ls target/webservices-*with-dependencies.jar | head -n1);
scp $JAR tezaurs: