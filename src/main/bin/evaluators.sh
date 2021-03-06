#!/bin/sh
#Shell script to solve some instances

#Usage
if [ $# -lt 2 ]; then
	echo "Usage: $0 input output ..."
	echo "\tinput: folder containing the instances json files"
	echo "\toutput: output folder for the resulting plan"
	echo "\tAdditional parameters will be passed to 'evaluator'"
	exit 1
fi

input=$1
output=$2
mkdir -p $output
shift
shift
for i in $(ls $input/*.json); do
	./bin/evaluator.sh -i $i -p $output/plan-$(basename $i .json).json -r $output/results.txt $*
done