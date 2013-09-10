The code in this repository aims at reproducing the experiments related to the following paper:

> Higher SLA Satisfaction in Datacenters. Huynh Tu Dang, Fabien Hermenier.
> Proceedings of "Workshop on Hot Topics in Dependable Systems 2013"

For any questions about the paper, the experiments, or basically anything
related to BtrPlace. see [the BtrPlace website](http://btrp.inria.fr).

## Building from sources ##

Requirements:
* JDK 7+
* maven 3+

The source of the released versions are directly available in the `Tag` section.
You can also download them using github features.
Once downloaded, move to the source directory then execute the following command
to make the distribution:

    $ mvn clean assembly:assembly -DskipTests

The resulting standalone distribution will be in `target/evaluation-1.0.tar.gz`

## Reproducing the results ##

To run the experiments, you must have a running Java 7 environment.

```
$ tree
 |- lib                          # dependencies
 |- LICENSE                      # LGPL v3 LICENSE
 |- README                       # This file
 |- hotdep-2013.tar.bz2          # default workload
 |- hotdep-2013-results.tar.bz2  # results for the default workload
 \- bin                          # helper scripts for Unix and R

#Extract the workload
$ tar xfz hotdep-2013.tar.bz2

#Run the evaluator. Here using dist_evaluators
#to process the workload on the 8 nodes
# listed in the `nodes` file
$ ./bin/dist_evaluators.sh nodes hotdep-2013 results

#Follow the progress
$ wc -l results/*/results.txt
     100 results/bs_continuous/results.txt
     100 results/bs_discrete/results.txt
     100 results/he_continuous/results.txt
     100 results/he_discrete/results.txt
     100 results/sf_continuous/results.txt
     100 results/sf_discrete/results.txt
     100 results/ve_continuous/results.txt
     100 results/ve_discrete/results.txt
     800 total
#Yeah ! 100 results per file. That's over

#Produce the datafile
$ ./bin/mergeResults.sh results/*/results.txt > results/allResults.txt

#Analyse the data using R
$ mkdir results/pdfs
$ ./bin/discrete_reports.R results/allResults.txt results/pdfs/distribution.pdf
$ ./bin/overhead.R results/allResults.txt results/pdfs
```

Additional `evaluator*` scripts are available in the `bin` directory. They
allow to process the workload at a finer grain.

## Generating a custom workload ##

This can be done using the `instanceMaker` and the `instancesMaker` scripts:

```
$ ./bin/instanceMaker
Missing required option: o
usage: InstanceMaker
 -a <arg>   number of applications
 -o <arg>   output JSON file
 -p <arg>   number of nodes per rack
 -r <arg>   number of racks
```

##Datafile format ##
The file is a CSV file with tabulations used as a separator.

1. Instance file name
2. Scenario type (ve, he, sf, bs)
3. Discrete (0) or Continuous (1) restriction
4. Solving status: -1<=> Unable to state about the feasibility, 0= No solution (proved), 1= At least 1 solution
5. Number of violated spread constraints
6. Number of violated among constraints
7. Number of violated splitAmong constraints
8. Number of violated singleResourceCapacity constraints
9. Number of violated MaxOnline constraints
10. Number of affected SLAs
11. Current CPU load
12. Current Memory load
13. CPU load once the plan is applied
14. Memory load once the plan is applied
15. core-RP building duration (ms.)
16. RP-specialisation duration (ms.)
17. solving duration (ms.)
18. Estimated plan duration (sec.)
19. Number of actions
20. Number of BootVM actions
21. Number of MigrateVM actions
22. Number of Allocate actions
23. Number of BootNode actions
24. Number of ShutdownNode actions