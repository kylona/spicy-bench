#!/bin/bash
if [ "$SPICY_BENCH_ROOT" -z ]; then
    SPICY_BENCH_ROOT=$1
fi
echo "Compiling Data Race Tool"
if ! javac -d $SPICY_BENCH_ROOT/src/jpf-hj/build/classes/ -cp $SPICY_BENCH_ROOT/lib/jgrapht-ext-0.9.1-uber.jar:$SPICY_BENCH_ROOT/lib/jpf.jar $SPICY_BENCH_ROOT/src/jpf-hj/comp_graph/cg/*.java
then
  echo "Compile of Tool Failed"
  exit 1
fi

