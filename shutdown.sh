#!/bin/bash

cd ${0%/*}

if [ -f RUNNING_PID ]; then
    cat RUNNING_PID | xargs kill
    echo "process killed"
else
    echo "already killed"
fi
