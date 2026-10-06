#!/bin/bash
# Runner script for Smart Digital Queue Management System
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

# Compile if classes do not exist
if [ ! -f "App.class" ]; then
    ./compile.sh
fi

# Run App (pass any args, like --cli)
java -cp "lib/*:." App "$@"
