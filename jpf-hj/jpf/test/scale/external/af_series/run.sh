mkdir build
mkdir results
PathToHJLib="$SPICY_BENCH_ROOT/HJLibFiles/lib/byu-hjlib.jar"
PathToOther="$SPICY_BENCH_ROOT/HJLibFiles/lib/jpf-extras.jar:$SPICY_BENCH_ROOT/HJLibFiles/lib/junit-4.12.jar:$SPICY_BENCH_ROOT/HJLibFiles/lib/jpf-classes.jar"
javac -cp $PathToHJLib:$PathToOther -d build $(find . -name "*.java")

name="af_series.JGFSeriesBenchSizeC"
NativeClassPath="$SPICY_BENCH_ROOT/jpf-hj/build/classes;$SPICY_BENCH_ROOT/jpf-hj/lib/jgrapht-ext-0.9.1-uber.jar"
PathToRunJPF="$SPICY_BENCH_ROOT/HJLibFiles/lib/RunJPF.jar"

echo "Running program natively..."
java -cp build:$PathToHJLib:$PathToOther $name > results/native.txt
echo "Saved output to results/native.txt."

echo "target=$name
Results_directory=results/
Contains_isolated_sections=true
On_the_fly=false
Data_race_detection=true
classpath=build:$PathToHJLib:$PathToOther
sourcepath=`pwd`
native_classpath+=$NativeClassPath
JPF.vm=VMListener
vm.scheduler.sharedness.class=extensions.HjSharednessPolicy
vm.scheduler.sync.class=extensions.HjSyncPolicy
listener+=cg.CGRaceDetector
vm.max_transition_length=MAX" > build/$name.jpf

echo "---------------------------------"
echo "Running data race detector..."
java -ea -jar $PathToRunJPF build/$name.jpf > results/output.txt
echo "Saved output to results/output.txt."
