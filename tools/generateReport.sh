#!/bin/bash
OUTPUT_NAME=output.txt
NO_MARKER="no errors detected"
TIME_MARKER="^real "
FINISH_MARKER="search finished"
parseOutput()
{
  DISPLAY_NAME=${1%/$OUTPUT_NAME}
  DISPLAY_NAME=${DISPLAY_NAME##*/}
  echo
  if grep -q "$NO_MARKER" "$1"  ; then
    echo $DISPLAY_NAME : "NO"
  else
    echo $DISPLAY_NAME : "YES"
  fi
  if grep -q "$FINISH_MARKER" "$1"  ; then
    grep $TIME_MARKER "$1"
  else
    echo "ERROR Exeption Thrown"
  fi
}
export -f parseOutput
find $1 -name $OUTPUT_NAME | while read file; do parseOutput "$file"; done
