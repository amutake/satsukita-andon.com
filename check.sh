#!/bin/bash

result="OK"

for file in $(find ./public/img/products -type f -print | sed -e "s/products/thumbnails/g"); do
    if [ ! -f $file ]; then
        echo "Missing: $file"
        result="Error"
    fi
done

echo $result
