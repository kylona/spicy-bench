#!/bin/bash
if [ -z $1 ] ; then
  echo "No Argument Given"
  declare -a NameArray
  again=0;
  while [ true ]; do

    for lost in $(find . -name '*.dot' );
      do
        contains=0;
        for found in ${NameArray[@]};
          do
            if [ $lost == $found ]; then
              contains=1
              break
            fi
        done
        if [ $contains  == 0 ]; then
          NameArray+=($lost);
          echo "$lost added to search group"
          (./copyWhenModify.sh $lost)&
        fi
    done
  done


else
id=0;
mkdir -p $1.changes
cp $1 $1.changes/changeNum$id
id+=1;
while [ true ]; do
while read j
do
   echo "$1 changed"
   echo "Copying to ./$1changes/changeNum$id"
   mkdir -p $1.changes
   echo made $1.changes directory
   cp $1 $1.changes/changeNum$id
   id+=1;
   break
done <  <(inotifywait -q -e modify $1 )
done
fi
