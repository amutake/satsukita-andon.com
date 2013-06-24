#!/bin/bash

cd ${0%/*}
if [ -f RUNNING_PID ]; then
    echo "already started"
else
    play clean compile stage && target/start -Dhttp.port=6039 &
fi
