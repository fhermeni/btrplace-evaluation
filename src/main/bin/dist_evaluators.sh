#!/bin/sh
#Dispatch full benchmark over 8 nodes
#ve, ve c, he, he c, sf, sf c, bs, bs c
if [ $# -ne 3 ]; then
	echo "Usage: $0 nodelist workload_path output"
	echo " nodelist: list of machines, 1 name per line"
	echo " workload_path: folder containing the workload. Passed to the 'evaluators' script"
	echo " output: folder where the results are stored"
	exit 1
fi
input=$2
output=$3
machines=$1

nb=1
cwd=$(pwd)
for scenario in ve he sf bs; do
	node=$(sed -n ${nb}p ${machines})
 	oarsh ${node} "cd ${cwd}; ./bin/evaluators.sh ${input} ${output}/${scenario}_discrete -s ${scenario}" &
	nb=$(($nb + 1))
	node=$(sed -n ${nb}p ${machines})
	oarsh ${node} "cd ${cwd}; ./bin/evaluators.sh ${input} ${output}/${scenario}_continuous -s ${scenario} -c" &
	nb=$(($nb + 1))
done
wait