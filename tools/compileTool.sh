#!/bin/bash

die_with_msg() {
  echo "$1"
  exit 1
}

[[ $# -eq 1 ]] || die_with_msg "Usage: compileTool.sh [cg|fasttrack|spbags]"

JARS="lib"
[[ -d "$JARS" ]] || die_with_msg "Could not find lib directory"

JAVAC_CP="jpf-hj/src:$JARS/jpf.jar:$JARS/jpf-hj.jar:$JARS/hj-lib-byu.jar:$JARS/jgrapht-ext-0.9.1-uber.jar"
echo "$JAVAC_CP"
DEST="build"
SOURCE_FILES=`find jpf-hj/src/extensions/$1 jpf-hj/src/extensions/util -type f -name "*.java"`

if [[ ! -d "$DEST" ]]
then
  read -p "$DEST directory does not exist in $(pwd), create? (y/n): "
  echo
  [[ "$REPLY" == "y" ]] || exit 1
  mkdir -p "$DEST"
fi

javac -d "$DEST" -cp "$JAVAC_CP" $SOURCE_FILES
