#!/bin/bash

cd ${0%/*}

if [ -f RUNNING_PID ]; then
    cat RUNNING_PID | xargs kill
    echo "kill process"
else
    echo "already killed"
fi
