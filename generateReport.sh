#!/bin/bash
OUTPUT_NAME=output.txt
YES_MARKER="Non-deterministic access between"
TIME_MARKER="GraphSize: "
FINISH_MARKER="search finished"
parseOutput()
{
  DISPLAY_NAME=${1%/$OUTPUT_NAME}
  DISPLAY_NAME=${DISPLAY_NAME##*/}
  echo
  if  grep -q "$YES_MARKER" "$1"  ; then
    echo $DISPLAY_NAME : "YES"
  else
    echo $DISPLAY_NAME : "NO"
  fi
  if  grep -q "$FINISH_MARKER" "$1"  ; then
    grep "$TIME_MARKER" "$1"
  else
    echo "ERROR Exeption Thrown"
  fi
}
export -f parseOutput
find $1 -name $OUTPUT_NAME | while read file; do parseOutput "$file"; done
