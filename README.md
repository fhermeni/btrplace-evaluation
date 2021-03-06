The code in this repository aims at reproducing the experiments related to the following paper:

> Higher SLA Satisfaction in Datacenters. Huynh Tu Dang, Fabien Hermenier.
> Proceedings of "Workshop on Hot Topics in Dependable Systems 2013"

For any questions about the paper, the experiments, or basically anything
related to BtrPlace. see [the BtrPlace website](http://btrp.inria.fr).

## Binary distribution ##

See http://btrp.inria.fr/repos/releases/btrplace/hotdep-2013/

## Building from sources ##

Requirements:
* JDK 7+
* maven 3+

The source of the released versions are directly available in the `Tag` section.
You can also download them using github features.
Once downloaded, move to the source directory then execute the following command
to make the distribution:

    $ mvn clean assembly:assembly -DskipTests

The resulting standalone distribution will be in `target/hotdep-2013-1.0-dist.tar.gz`

## Reproducing the results ##

To run the experiments, you must have a running Java 7 environment.

```
#Download and extract the [workload](http://btrp.inria.fr/hotdep-2013-workload.tar.bz2)
#from [BtrPlace Website](http://btrp.inria.fr)

#Run the evaluator. Here using dist_evaluators
#to process the workload 'hotdep-2013-workload' on the 8 nodes
# listed in the 'nodes' file
$ ./bin/dist_evaluators.sh nodes hotdep-2013-workload results

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
#Yeah! 100 results per file. The evaluation is finished

#Produce the datafile
$ ./bin/mergeResults.sh results/*/results.txt > results/allResults.txt

#Analyse the data using R
$ ./bin/discrete_reports.R results/allResults.txt results/distribution.pdf
$ ./bin/overhead.R results/allResults.txt results/
```

Additional `evaluator*.sh` scripts are available in the `bin` directory. They
allow to process the workload at a finer grain.

The raw results used in the paper is also available [online](http://btrp.inria.fr/pubs/hotdep-2013-results.tar.bz2),
from the [BtrPlace Website](http://btrp.inria.fr).
## Generating a custom workload ##

This can be done using the `bin/instanceMaker.sh` and the `bin/instancesMaker.sh` scripts:

```
$ ./bin/instanceMaker.sh
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
2. Scenario type: 0=ve, 1=he, 2=sf, 3=bs
3. Discrete (0) or Continuous (1) restriction
4. Solving status: -1= Unable to state about the feasibility, 0= No solution (proved), 1= At least 1 solution
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