#!/bin/bash

VERSION="4.0.0"
NAME="quiz"

cd ${0%/*}

zip -r ${NAME}.zip * -x generate.sh *.zip
curl -o generated.zip -F "file=@./${NAME}.zip" http://quizgenerator.net/quizgen${VERSION}/index.php?action=generate
rm ${NAME}.zip
rm -r ../../public/quiz/*
mv generated.zip ../../public/quiz/
cd ../../public/quiz/
unzip generated.zip
rm generated.zip
type open && open index.html
