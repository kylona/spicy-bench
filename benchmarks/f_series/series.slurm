#!/bin/bash
#SBATCH --job-name=series
#SBATCH --partition=commons
#SBATCH --nodes=1
#SBATCH --exclusive
#SBATCH --cpus-per-task=16
#SBATCH --ntasks-per-node=1
#SBATCH --mem-per-cpu=2G
#SBATCH --time=05:00:00
#SBATCH --mail-user=rishi@rice.edu
#SBATCH --mail-type=ALL
echo "My job ran on:"
echo $SLURM_NODELIST
./unsafe.sh
