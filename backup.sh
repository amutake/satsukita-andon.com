#!/bin/bash

cd ${0%/*}

now=`date +%Y-%m-%d`

echo 'compress files...'
tar czf backup/${now}.tar.gz ./files
echo 'done.'

./shutdown.sh

echo 'dump sql...'
cd db
./backup.sh
mv backup.sql ../backup/${now}.sql
echo 'done.'

cd ../
./startup.sh
