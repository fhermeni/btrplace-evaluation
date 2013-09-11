#!/bin/sh
#Aggregate the resulting statistics to make
#them processable by R scripts

echo "instance\tscenario\tcontinuous\tstatus\tspread\tamong\tsplitAmong\tsingleResourceCapacity\tmaxOnline\tviolatedSLAs\tcurCPU\tcurMem\tnextCPU\tnextMem\tcoreRP\tspeRP\tcomputationDuration\tapplyDuration\tactions\tbootVM\tmigrateVM\tAllocate\tbootNode\tshutdownNode"
while [ ! -z "$1" ]; do
	cat $1
	shift
done
