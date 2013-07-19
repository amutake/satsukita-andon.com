#!/bin/bash

java -cp h2.jar org.h2.tools.Script -url jdbc:h2:default -user "" -password "" -script backup.sql
