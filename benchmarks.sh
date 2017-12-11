#!/bin/bash

mkdir temp
mkdir temp/array.PrimitiveArrayRace
mkdir temp/PrimitiveArrayRaceless
mkdir temp/array.PrimitiveArrayRaceless
mkdir temp/permission.conference.StringTest
mkdir temp/benchmarks.examples.ReciprocalArraySum
mkdir temp/permission.array.PrimitiveArrayNoRace
mkdir temp/array.TwoDimArrays
mkdir temp/benchmarks.COMP322.ForallWithIterable
mkdir temp/benchmarks.COMP322.IntegerCounterIsolated
mkdir temp/benchmarks.COMP322.PipelineWithFutures
mkdir temp/benchmarks.examples.BinaryTrees
mkdir temp/benchmarks.examples.PrimeNumCounter
mkdir temp/benchmarks.examples.PrimeNumCounterForAll
mkdir temp/benchmarks.examples.PrimeNumCounterForAsync
mkdir temp/benchmarks.matrix.Add
mkdir temp/benchmarks.matrix.ScalarMultiply
mkdir temp/benchmarks.matrix.VectorAdd
mkdir temp/benchmarks.examples.ClumpedAcess


./runOne.sh array.PrimitiveArrayRace temp/array.PrimitiveArrayRace > temp/array.PrimitiveArrayRace/output.txt
./runOne.sh array.PrimitiveArrayRaceless temp/array.PrimitiveArrayRaceless > temp/array.PrimitiveArrayRaceless/output.txt
./runOne.sh permission.conference.StringTest temp/permission.conference.StringTest > temp/permission.conference.StringTest/output.txt
./runOne.sh benchmarks.examples.ReciprocalArraySum temp/benchmarks.examples.ReciprocalArraySum > temp/benchmarks.examples.ReciprocalArraySum/output.txt 
./runOne.sh permission.array.PrimitiveArrayNoRace temp/permission.array.PrimitiveArrayNoRace > temp/permission.array.PrimitiveArrayNoRace/output.txt 
./runOne.sh array.TwoDimArrays temp/array.TwoDimArrays > temp/array.TwoDimArrays/output.txt 
./runOne.sh benchmarks.COMP322.ForallWithIterable temp/benchmarks.COMP322.ForallWithIterable > temp/benchmarks.COMP322.ForallWithIterable/output.txt 
./runOne.sh benchmarks.COMP322.IntegerCounterIsolated temp/benchmarks.COMP322.IntegerCounterIsolated > temp/benchmarks.COMP322.IntegerCounterIsolated/output.txt 
./runOne.sh benchmarks.COMP322.PipelineWithFutures temp/benchmarks.COMP322.PipelineWithFutures > temp/benchmarks.COMP322.PipelineWithFutures/output.txt 
./runOne.sh benchmarks.examples.BinaryTrees temp/benchmarks.examples.BinaryTrees > temp/benchmarks.examples.BinaryTrees/output.txt 
./runOne.sh benchmarks.examples.PrimeNumCounter temp/benchmarks.examples.PrimeNumCounter > temp/benchmarks.examples.PrimeNumCounter/output.txt 
./runOne.sh benchmarks.examples.PrimeNumCounterForAll temp/benchmarks.examples.PrimeNumCounterForAll > temp/benchmarks.examples.PrimeNumCounterForAll/output.txt 
./runOne.sh benchmarks.examples.PrimeNumCounterForAsync temp/benchmarks.examples.PrimeNumCounterForAsync > temp/benchmarks.examples.PrimeNumCounterForAsync/output.txt 
./runOne.sh benchmarks.matrix.Add temp/benchmarks.matrix.Add > temp/benchmarks.matrix.Add/output.txt 
./runOne.sh benchmarks.matrix.ScalarMultiply temp/benchmarks.matrix.ScalarMultiply > temp/benchmarks.matrix.ScalarMultiply/output.txt 
./runOne.sh benchmarks.matrix.VectorAdd temp/benchmarks.matrix.VectorAdd > temp/benchmarks.matrix.VectorAdd/output.txt 
./runOne.sh benchmarks.examples.ClumpedAcess temp/benchmarks.examples.ClumpedAcess > temp/benchmarks.examples.ClumpedAcess/output.txt 
# scale/external/af_crypt/
# scale/external/f_crypt/
# scale/external/af_series/
# scale/external/f_series/
# scale/external/strassen
