#!/bin/bash

cd ${0%/*}

./shutdown.sh

now=`date +%Y-%m-%d`

echo 'compress files...'
tar czf backup/${now}.tar.gz ./files
echo 'done.'

echo 'dump sql...'
cd db
./backup.sh
mv backup.sql ../backup/${now}.sql
echo 'done.'

cd ../
./startup.sh
