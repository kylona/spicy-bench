# spicy-bench

A repository of this https://github.com/LLNL/dataracebench.git converted into Habanero-Java

# Usage

The benchmarks are currently being translated.

Running the scripts `scripts/runOne.sh` and `scripts/runAll.sh` currently targets the
benchmarks held in `benchmarks/AllBenchmarks2018` directory.

To run one benchmark, type this into your shell from this directory.

`tools/runOne.sh [BenchmarkClass] [race-detector] [path/to/jpf]`

Replace `[BenchmarkClass]` with a class name in the `benchmarks/AllBenchmarks2018` directory
and `[race-detector]` with the name of one of the JPF config files located in the `config`
directory. The path to a local JPF repository is used to execute `$JPF/bin/jpf`.

For example,

`tools/runOne.sh Add cg src/jpf/jpf-core`

You will have to make sure the race detector you are trying to run has been compiled.
The second argument to the script is the build directory of the JPF to compile against.
It should be the same one that is pointed to by your `$HOME/.jpf/site-properties`.

`tools/compileTool.sh cg src/jpf/jpf-core`
