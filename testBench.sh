#!/bin/bash

#reads in class names form classNames.txt in same directory
#searches for source code, compiles and creates .jpf
#it then times each of the classes nativly and in the datarace detector
rm -r Classes
TimeStoreFile=TimesRecorded.txt
FindClassNames=classNames.txt
PathToHJlib='HJLibFiles/lib/byu-hjlib.jar'
#PathToHJlib='HJLibFiles/lib/hj-lib-byu.jar'
PathTORunJPF="HJLibFiles/lib/RunJPF.jar"
NativeClassPath="/home/kylona/workspace/spicy-bench/jpf-hj/build/classes;/home/kylona/workspace/spicy-bench/jpf-hj/lib/jgrapht-ext-0.9.1-uber.jar"
PathToClasses='Classes'
mkdir -p $PathToClasses

while fileOfClassNames='' read -r name || [[ -n "$name" ]]; do
    FoundJava=$(find -name "$name.java" -print -quit)
    if [ -z $FoundJava ]; then
    echo 'Could Not find source for' $name
    continue
    fi
    if [ ! -f $PathToClasses/$name.class ]; then
      echo "Compiling: $FoundJava"
      if ! javac -cp $PathToClasses:$PathToHJlib -d $PathToClasses $FoundJava; then
        echo 'Compile Step Failed'
        exit 1
      fi
      echo "Making $name.jpf"
      echo target=$name > $PathToClasses/$name.jpf
      echo classpath=$PathToClasses:$PathToHJlib >> $PathToClasses/$name.jpf
      echo sourcepath=$(dirname $FoundJava) >> $PathToClasses/$name.jpf
      echo native_classpath=$NativeClassPath >> $PathToClasses/$name.jpf
      echo vm.scheduler.sharedness.class=extensions.HjSharednessPolicy >> $PathToClasses/$name.jpf
      echo vm.scheduler.sync.class=extensions.HjSyncPolicy >> $PathToClasses/$name.jpf
      echo listener+=extensions.HjListener, >> $PathToClasses/$name.jpf
      echo listener+=extensions.StateGraphListener, >> $PathToClasses/$name.jpf
      echo listener+=cg.CGRaceDetector >> $PathToClasses/$name.jpf
      echo vm.max_transition_length=MAX >> $PathToClasses/$name.jpf
    fi
done < $FindClassNames

echo "Time taken for each test file: " > $TimeStoreFile
echo >> $TimeStoreFile

while fileOfClassNames='' read -r name || [[ -n "$name" ]]; do
    echo
    echo
    echo | tee -a $TimeStoreFile
    echo '<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>'
    echo "Testing: $name" | tee -a $TimeStoreFile
    echo ""
    /usr/bin/time -ao $TimeStoreFile -f "Native Running Time: %e seconds" java -cp $PathToClasses:$PathToHJlib $name
    echo "----------------------------------------------"
    /usr/bin/time -ao $TimeStoreFile -f "Test Running Time: %e seconds" java -jar $PathTORunJPF $PathToClasses/$name.jpf
done < $FindClassNames
echo '<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>'

cat $TimeStoreFile
