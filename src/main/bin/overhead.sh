#!/bin/sh
#Generate the plots that depicts the performance overhead of continuous
#placement constraints

if [ "$#" -ne 2 ]; then
	echo "Usage: $0 datafile output_dir"
	echo "datafile: the resulting data"
	echo "output_dir: where 've.pdf', 'bs.pdf', 'sf.pdf' and 'he.pdf' will be stored"
	exit 1
fi

#Generate each graph
for s in ve he bs sf; do
	./bin/${s}.R $1 $2/${s}.pdf
done