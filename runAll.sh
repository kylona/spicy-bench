#!/bin/bash

sh compileTool.sh

# Gather the list of benchmarks
# Run all of them using the runOne.sh script

benchmarks=(HJLibFiles/src/*)
destination="started-`date +"%F_%H-%M-%S"`"

for b in "${benchmarks[@]}"; do
    basename=$(basename "$b")
    filename="${basename%.*}"
    resultsFolder="/home/jpf/$destination/datarace-results_`date +"%F_%H-%M-%S"`"
    mkdir -p $resultsFolder
    echo "Running $filename..."
    sh runOne.sh $filename $resultsFolder > $resultsFolder/output.txt 
    echo "Done. Output saved to $resultsFolder/output.txt"
done