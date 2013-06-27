#!/bin/bash

n=`expr $1 % 10`

if [ $n = "1" ]; then
    ORD="${1}st"
elif [ $n = "2" ]; then
    ORD="${1}nd"
elif [ $n = "3" ]; then
    ORD="${1}rd"
else
    ORD="${1}th"
fi

path="./products/${ORD}/3/${2}/${ORD}3-${2}_01.jpg"
path2="./grands/${ORD}.jpg"

echo $path
echo $path2
cp $path $path2
