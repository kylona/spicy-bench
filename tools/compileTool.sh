#!/bin/bash

die_with_msg() {
  echo "$1"
  exit 1
}

[[ $# -eq 2 ]] || die_with_msg "Usage: compileTool.sh [cg|fasttrack|spbags] path/to/jpf-core"

JPF_JARS="$2/build"
JARS="lib"
[[ -d "$JARS" ]] || die_with_msg "Could not find lib directory"
[[ -d "$JPF_JARS" ]] || die_with_msg "Could not find JPF lib directory"

JAVAC_CP="src/jpf/jpf-$1:$JPF_JARS/jpf.jar:$JARS/jpf-hj.jar:$JARS/hj-lib-byu.jar:$JARS/jgrapht-ext-0.9.1-uber.jar"
echo "$JAVAC_CP"
DEST="build"
SOURCE_FILES=`find src/jpf/jpf-$1 -name "test" -prune -o -type f -name "*.java" -print`

if [[ ! -d "$DEST" ]]
then
  read -p "$DEST directory does not exist in $(pwd), create? (y/n): "
  echo
  [[ "$REPLY" == "y" ]] || exit 1
  mkdir -p "$DEST"
fi

javac -d "$DEST" -cp "$JAVAC_CP" $SOURCE_FILES
