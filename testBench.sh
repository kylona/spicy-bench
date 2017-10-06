#!/bin/bash

#reads in class names form classNames.txt in same directory
#searches for source code, compiles and creates .jpf
#it then times each of the classes nativly and in the datarace detector

Pathlib='HJLibFiles/lib'
PathTORunJPF="HJLibFiles/lib/RunJPF.jar"
PathClass='Classes:'$Pathlib'/hj-lib-byu.jar:'$Pathlib'/hamcrest-core-1.3.jar:'$Pathlib'/junit-4.12.jar'
echo $PathClass
PathToClasses='Classes'
mkdir $PathToClasses

while fileOfClassNames='' read -r name || [[ -n "$name" ]]; do
    FoundJava=$(find -name "$name.java" -print -quit)
    if [ ! -f $PathToClasses/$name.class ]; then
      echo "Compliling: $name"
      javac -cp $PathClass -d $PathToClasses $FoundJava
      echo "Making $name.jpf"
      echo target=$name > $PathToClasses/$name.jpf
      echo classpath=$PathClass >> $PathToClasses/$name.jpf
      echo sourcepath=$(dirname $FoundJava) >> $PathToClasses/$name.jpf

    fi
done < classNames.txt

while fileOfClassNames='' read -r name || [[ -n "$name" ]]; do
    echo
    echo
    echo
    echo '<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>'
    echo "Testing: $name"
    echo ""
    /usr/bin/time -f "Native Running Time: %e seconds" java -cp $PathClass $name
    echo "----------------------------------------------"
    /usr/bin/time -f "Test Running Time: %e seconds" java -jar $PathTORunJPF $PathToClasses/$name.jpf
done < classNames.txt
echo '<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>'
