#!/bin/bash

if [ $1 ]; then
    echo "optimize $1 dir. OK? [y/n] "
    read ok
    if [ "$ok" = "y" ]; then
        find $1 -type f -name "*.jpg" | xargs jpegoptim --strip-all --max=50
        echo "done."
    else
        echo "abort."
    fi
else
    echo "optim <dir-path>"
fi
