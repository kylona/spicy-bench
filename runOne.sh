#!/bin/bash

RESULTS_FOLDER="$2"
mkdir "$RESULTS_FOLDER/build"
mkdir "$RESULTS_FOLDER/results"

# some prereqs for jpf & hjlib
PathToHJLib="HJLibFiles/lib/byu-hjlib.jar"
PathToRunJPF="HJLibFiles/lib/RunJPF.jar"
PathToOther="HJLibFiles/lib/jpf-extras.jar:HJLibFiles/lib/junit-4.12.jar:HJLibFiles/lib/jpf-classes.jar"
LibPath=$SPICY_BENCH_ROOT/jpf-hj/lib
NativeClassPath="$SPICY_BENCH_ROOT/jpf-hj/build/classes;$SPICY_BENCH_ROOT/jpf-hj/lib/jgrapht-ext-0.9.1-uber.jar"

# try to find any java file with the name supplied in the first argument under the current directory anywhere
name="$1"
src=${name##*.}
FoundJava=$(find . -path ./jpf/jpf-core/src/tests -prune -o -name "$src.java" -print -quit)
if [ -z $FoundJava ]; then
  echo 'Could not find source for' $name
  exit 1
fi

# compile the classes and build the jpf configuration
echo "Compiling: $FoundJava"
if ! javac -cp $PathToHJLib:$PathToOther -d "$RESULTS_FOLDER/build" $FoundJava; then
  echo 'Compile Step Failed'
  exit 1
fi
echo "Making $name.jpf"

echo "target=$name
Results_directory=$RESULTS_FOLDER/results/
Contains_isolated_sections=true
On_the_fly=false
Data_race_detection=true
classpath=$RESULTS_FOLDER/build:$PathToHJLib:$PathToOther
sourcepath=$(dirname $FoundJava)
native_classpath+=$NativeClassPath
JPF.vm=VMListener
vm.scheduler.sharedness.class=extensions.HjSharednessPolicy
vm.scheduler.sync.class=extensions.HjSyncPolicy
listener+=cg.CGRaceDetector
vm.max_transition_length=MAX" > $RESULTS_FOLDER/build/$name.jpf

# now, run the data race detector
/usr/bin/time -f "Native Running Time: %e seconds" java -cp $RESULTS_FOLDER/build:$PathToHJLib:$PathToOther $name
echo "----------------------------------------------"
#/var/jpf-core/bin/jpf $RESULTS_FOLDER/build/$name.jpf
java -ea -jar $PathToRunJPF $RESULTS_FOLDER/build/$name.jpf
