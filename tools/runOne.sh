#!/bin/bash

die_with_msg() {
  echo "$1"
  exit 1
}

[[ $# -eq 2 ]] || die_with_msg "Usage: runOne.sh benchmark detector"

BENCHMARK="$1"
DETECTOR="$2"

SRC="benchmarks/"
LIB_CP="lib/byu-hjlib.jar:lib/hj-lib-byu.jar:lib/jpf-hj.jar"
JAVAC_CP="$SRC:$LIB_CP"
CONFIG_DIR="config"

OUTPUT_DIR="build"
CLASSES_DIR="$OUTPUT_DIR/benchmarks"

# try to find any java file with the name supplied in the first argument
name=${BENCHMARK##*.}
SRC_FILE=$(find "$SRC" -name "$name.java" -print -quit)
[ -f "$SRC_FILE" ] || die_with_msg "Could not find source for $BENCHMARK"

# try to find extension.jpf
DETECTOR_JPF=$(find "$CONFIG_DIR" -name "$DETECTOR.jpf" -print -quit)
[ -f "$DETECTOR_JPF" ] || die_with_msg "Could not find jpf config for $DETECTOR"

mkdir -p "$CLASSES_DIR"

# compile the classes and build the jpf configuration
echo "Compiling: $SRC_FILE"
if ! javac -cp $JAVAC_CP -d $CLASSES_DIR $SRC_FILE; then
  die_with_msg "Compilation failed"
fi

echo "Making $name.jpf"
sed "s/SED_WILL_REPLACE/$BENCHMARK/" "$DETECTOR_JPF" > "$OUTPUT_DIR/$name.jpf"

JAVAC_CP="$CLASSES_DIR:$LIB_CP"
# now, run the data race detector
time java -cp $JAVAC_CP $BENCHMARK
echo "----------------------------------------------"
time timeout -s 9 1h java -ea -jar "lib/RunJPF.jar" $OUTPUT_DIR/$name.jpf
