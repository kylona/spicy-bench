#!/bin/bash

JARS="lib"
[[ -d "$JARS" ]] || die_with_msg "Could not find lib directory"

JAVAC_CP="jpf-hj/src:$JARS/jpf.jar:$JARS/jpf-hj.jar:$JARS/hj-lib-byu.jar:$JARS/jgrapht-ext-0.9.1-uber.jar"
echo "$JAVAC_CP"
DEST="build"
SOURCE_FILES=`find jpf-hj/src/extensions/cg jpf-hj/src/extensions/compgraph jpf-hj/src/extensions/fasttrack jpf-hj/src/extensions/util jpf-hj/src/extensions/zipper -type f -name "*.java"`

if [[ ! -d "$DEST" ]]
then
  read -p "$DEST directory does not exist in $(pwd), create? (y/n): "
  echo
  [[ "$REPLY" == "y" ]] || exit 1
  mkdir -p "$DEST"
fi

javac -d "$DEST" -cp "$JAVAC_CP" $SOURCE_FILES
