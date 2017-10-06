#!/bin/bash

PathToHJlib='HJLibFiles/lib/hj-lib-byu.jar'
PathTORunJPF="HJLibFiles/lib/RunJPF.jar"
PathToClasses='Classes'
mkdir $PathToClasses

echo Not Working Yet...

while fileOfClassNames='' read -r name || [[ -n "$name" ]]; do
    FoundJava=$(find -name "$name.java" -print -quit)
    if [ ! -f $PathToClasses/$name.class ]; then
      echo "Compliling: $name"
      javac -cp $PathToClasses:$PathToHJlib -d $PathToClasses $FoundJava
      echo "Making $name.jpf"
      echo target=$name > $PathToClasses/$name.jpf
      echo classpath=$PathToClasses:$PathToHJlib >> $PathToClasses/$name.jpf
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
    /usr/bin/time -f "Native Running Time: %e seconds" java -cp $PathToClasses:$PathToHJlib $name
    echo "----------------------------------------------"
    /usr/bin/time -f "Test Running Time: %e seconds" java -jar $PathTORunJPF $PathToClasses/$name.jpf
done < classNames.txt
echo '<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>'
