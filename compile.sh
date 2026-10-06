#!/bin/bash
# Compilation script for Smart Digital Queue Management System
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

echo "============================================="
echo " Compiling Smart Digital Queue System..."
echo "============================================="

javac -cp "lib/*:." \
    model/*.java \
    db/*.java \
    dao/*.java \
    service/*.java \
    ui/*.java \
    Customer.java \
    QueueManager.java \
    main.java \
    App.java

echo "Compilation successful! Ready to run."
