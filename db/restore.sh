#!/bin/bash

java -cp h2.jar org.h2.tools.RunScript -url jdbc:h2:default -user "" -password "" -script ${1}.sql
