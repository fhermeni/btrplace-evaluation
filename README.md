#Evaluation of continuous satisfaction of the constraints in BtrPlace

## Introduction

The evaluation contain:  
* Model & Constraints Generator  
* Benchmark  
* PlanChecker  

**MCGenerator:**  Generates a model and a constraint associates with the model. One can specify the number of nodes and VMs in the model. Furthermore, for some constraints need a set of VM or a set of Node, this can be done by passing numbers in command's parameters.

**Benchmark:** fixes the model if it doesn't satisfy the constraint, then the benchmark creates the increase in workload of VM by add the Preserve constraints on the set of VMs invloved in the tested constraints.

**PlanChecker:** Use to check whether the plan computed in the discrete satisfaction of the constraints satisfies their continuous satisfaction.

## Usage

**MCGenerator** -n #node -m #vm [-ci] [-t type] [-s sizeVmSet] [-z sizeNodeSet]  
-c  Produce continuous satisfaction constraint  
-i  Produce the model with identical nodes and VMs  
-n  Number of nodes in the model  
-m  Number of VMs in the model  
-t  Type of the constraint  
-s  Size of a set of VM  
-z  Size of a set of node  

  **Example:** MCGenerator -n 100 -m 500 -i -t spread -s 40  
Produce a model of 100 nodes and 500 VMs and a Spread constraint including 40 VMs.

**Benchmark** [-c] [-e event] [-i percent] [-o output] -m model constraints  
-c  Find the reconfiguration plan with continuous satisfaction  
-e  The Event Type causes the consolidation. It consists of [failure, load, ...]  
-i  In case of event load, this option indicates how many percents of increasing load  
-o  Set the output plan destination  
-m  The model to reconfigure  
constraints: sequence listed constraints to be satisfied in the reconfiguration  

**PlanChecker** [-c] -p plan constraints  
-c  Check the continuous satisfaction of the constraints  
-p  The plan to be checked  
constraints: sequence listed constraints to be considered
