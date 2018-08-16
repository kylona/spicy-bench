#!/bin/bash

# Gather the list of benchmarks
# Run all of them using the runOne.sh script

die_with_msg() {
  echo "$1"
  exit 1
}

[[ $# -eq 2 ]] || die_with_msg "Usage: runAll.sh detector path/to/jpf-core"

detector="$1"
benchmarks=(benchmarks/AllBenchmarks2018/*)
destination="started-`date +"%F_%H-%M-%S"`"

for b in "${benchmarks[@]}"; do
    basename=$(basename "$b")
    filename="${basename%.*}"
    resultsFolder="build/$destination/$filename"
    mkdir -p $resultsFolder
    echo "Running $filename..."
    sh tools/runOne.sh $filename $detector $2 > $resultsFolder/output.txt 2>&1
    echo "Done. Output saved to $resultsFolder/output.txt"
done
