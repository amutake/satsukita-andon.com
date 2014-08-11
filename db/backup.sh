#!/bin/bash

cp default.h2.db backup.h2.db
java -cp h2.jar org.h2.tools.Script -url jdbc:h2:backup -user "" -password "" -script backup.sql
rm backup.h2.db
