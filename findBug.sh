#!/bin/bash
rm -r saveDotTmp
mkdir saveDotTmp
cp Classes/Results/* saveDotTmp
rm -r Classes
PathToHJlib='HJLibFiles/lib/byu-hjlib.jar'
PathTORunJPF="HJLibFiles/lib/RunJPF.jar"
NativeClassPath="/home/kylona/workspace/spicy-bench/jpf-hj/build/classes;/home/kylona/workspace/spicy-bench/jpf-hj/lib/jgrapht-ext-0.9.1-uber.jar"
PathToClasses='Classes'
mkdir -p $PathToClasses
name=DataRaceIsolateSimple1
FoundJava=$(find -name "$name.java" -print -quit)
if [ -z $FoundJava ]; then
  echo 'Could Not find source for' $name
  exit 1
fi
if [ ! -f $PathToClasses/$name.class ]; then
  echo "Compiling: $FoundJava"
  if ! javac -cp $PathToClasses:$PathToHJlib -d $PathToClasses $FoundJava; then
    echo 'Compile Step Failed'
    exit 1
  fi

  echo "Making $name.jpf"
  echo target=$name > $PathToClasses/$name.jpf
  echo Results_directory =$PathToClasses/Results/ >> $PathToClasses/$name.jpf
  echo Contains_isolated_sections = true >> $PathToClasses/$name.jpf
  echo On_the_fly = false >> $PathToClasses/$name.jpf
  echo Data_race_detection = true >> $PathToClasses/$name.jpf
  echo classpath=$PathToClasses:$PathToHJlib >> $PathToClasses/$name.jpf
  echo sourcepath=$(dirname $FoundJava) >> $PathToClasses/$name.jpf
  echo native_classpath+=$NativeClassPath >> $PathToClasses/$name.jpf
  echo JPF.vm=VMListener >> $PathToClasses/$name.jpf
  echo vm.scheduler.sharedness.class=extensions.HjSharednessPolicy >> $PathToClasses/$name.jpf
  echo vm.scheduler.sync.class=extensions.HjSyncPolicy >> $PathToClasses/$name.jpf
  #echo listener+=extensions.HjListener, >> $PathToClasses/$name.jpf
  #echo listener+=extensions.StateGraphListener, >> $PathToClasses/$name.jpf
  echo listener+=cg.CGRaceDetector >> $PathToClasses/$name.jpf
  echo vm.max_transition_length=MAX >> $PathToClasses/$name.jpf
fi

echo "Compiling Data Race Tool"
if ! javac -d ~/workspace/spicy-bench/jpf-hj/build/classes/ -cp ~/workspace/spicy-bench/jpf-hj/lib/jgrapht-ext-0.9.1-uber.jar:/home/kylona/workspace/spicy-bench/jpf-hj/lib/jpf.jar ~/workspace/spicy-bench/jpf-hj/comp_graph/cg/*.java
then
  echo "Compile of Tool Failed"
  exit 1
fi
/usr/bin/time -f "Native Running Time: %e seconds" java -cp $PathToClasses:$PathToHJlib $name
echo "----------------------------------------------"
java -jar $PathTORunJPF $PathToClasses/$name.jpf
//rdfind -deleteduplicates true Classes/Results/

echo Diff of files:
diff -r saveDotTmp Classes/Results
