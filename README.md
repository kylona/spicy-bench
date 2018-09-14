# spicy-bench

A repository of this https://github.com/LLNL/dataracebench.git converted into Habanero-Java

# Usage

The benchmarks are currently being translated.

Running the scripts `scripts/runOne.sh` and `scripts/runAll.sh` currently targets the
benchmarks held in `benchmarks/AllBenchmarks2018` directory.

To run one benchmark, type this into your shell from this directory.

`tools/runOne.sh [BenchmarkClass] [race-detector]`

Replace `[BenchmarkClass]` with a class name in the `benchmarks/AllBenchmarks2018` directory
and `[race-detector]` with the name of one of the JPF config files located in the `config`
directory.

For example,

`tools/runOne.sh Add fasttrack`

You will have to make sure the race detector you are trying to run has been compiled.

`tools/compileTool.sh`
