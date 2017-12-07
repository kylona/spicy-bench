#!/bin/bash
OUTPUT_NAME=output.txt
YES_MARKER="Data Race detected"
TIME_MARKER="elapsed time:       "
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
  grep "$TIME_MARKER" "$1"
  echo
}
export -f parseOutput
find $1 -name $OUTPUT_NAME | while read file; do parseOutput "$file"; done
