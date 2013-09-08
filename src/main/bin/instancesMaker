#!/bin/sh
#Shell script to generate multiple instances

if [ $# -lt 2 ]; then
	echo "Usage: $0 nb output ..."
	echo "\tnb: number of instances"
	echo "\toutput: the output folder"
	echo "\tAdditional parameters will be passed to 'instanceMaker'"
	exit 1
fi
nb=$1
output=$2
mkdir -p $output
shift
shift
for i in $(seq 1 $nb); do
	./instanceMaker -o $output/$i.json $*
	echo "Instance $i stored in $output/$i.json"
done