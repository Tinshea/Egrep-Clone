#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Usage: ./launch.sh <pattern> <file>"
    exit 1
fi

PATTERN=$1
FILE=$2

javac -d . Automate/*.java && javac -d . KMPAlgorithm/*.java 

if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi

java Automate.RegEx "$PATTERN" "$FILE"
