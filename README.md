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

```
$ tree
 |- lib                          # dependencies
 |- LICENSE                      # LGPL v3 LICENSE
 |- README                       # This file
 |- hotdep-2013.tar.bz2          # default workload
 |- hotdep-2013-results.tar.bz2  # results for the default workload
 \- bin                          # helper scripts for Unix and R
```

1. Extract the workload:
  `$ tar xjf hotdep-2013.tar.bz2`
2. Run the evaluator. Here using 8 machines listed in a `nodes` file
   `$ ./bin/dist_evaluators.sh hotdep-2013 results`
   You can follow the progress by looking at the number of results stored in the `results` folder using `$ wc -l results/*/results.txt`
   The evaluation will be finished once there is 100 lines per file
3. Produce the datafile
   `$ ./bin/mergeResults.sh results/*/results.txt > results/allResults.txt`
4. Analyse the data. This requires a [R](http://www.r-project.org/) distribution
```
$ mkdir results/pdfs
$ ./bin/discrete_report.R results/allResults.txt results/pdfs/distribution.pdf
$ ./bin/overhead.R results/allResults.txt results/pdfs
```

For a more fine grain evaluation, refer to the multiple `evaluate*` scripts in the `bin` directory

## Generating a custom workload ##

This can be done using the `instanceMaker` and the `instancesMaker` scripts:

```
$ ./instanceMaker
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
