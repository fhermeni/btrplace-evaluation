## Introduction

[BtrPlace](http://btrp.inria.fr) is a virtual machines placement algorithm for hosting platforms. It can be customized
by administrators to ensure the satisfaction of SLAs and to perform the administrative management operations.

In this project, we evaluate the level of satisfaction of the constraint during the reconfiguration of datacenters due
to the hardware failures or increasing in the workload of VMs.


#Evaluation Protocol
To evaluation Btrplace, We use the specification of the commodity server. The work-node specification details are listed below.

**Datacenter configuration**   
The datacenter consists of 256 working nodes mounted equally in 16 racks. The datacenter is divided into 2 zones each contains 8 racks. 
In this datacenter, we run 350 3-tiers applications that have between 6 and 18 VMs, with 2 to 6 VMs per tier.

For instance, a 3-tier application has 2 VMs for the Presentation Layer,
4 VMs for Business Logic and Data Layers.


**Datacenter consolidation scenarios**  

1. Vertical Scaling  
We simulate the increasing load of the applications by preserving more CPU/RAM for some arbitrary VMs until it
causes the consolidation happens. 

2. Horizontal Scaling  
Application's workload increases, more VMs needed for each tier to handle the increasing load. The set of constraints of the application then include
the cloned VMs. 

3. Hardware Failure / Network Maintenance  
The average rate of hardware failure in a datacenter is around 5% at any given moment. The network maintenance operation also brings down some nodes.
We simulate this event by turning off random nodes in the datacenter. 

4. BootStorm VM  
There are some moments during a working day, hundreds of VMs are powered on / off simultaneously (the beginning and the end of a working day). It may
affect the performance of the applications. The consolidation manager need to reconfigure the datacenter for load balancing and satisfaction of SLAs.

**Evaluation**
In each scenario, we record the reconfiguration plans computed by BtrPlace both for discrete restriction
and continuous restriction of the constraints. 
We count the number of SLAs temporary violated, particularly the constraints composing the SLAs are violated.
After that, we compare the time needed to compute the plans, and to
complete the reconfiguration process. Additionally, we compare the number of actions and the dependency between the action
specified in the plans.

## Tools

The evaluation contains:
* ModelMaker
* Evaluator

**instanceMaker:**  Generates a model and constraints associates with the model. Each application contains 3 Spread constraints (1 per tier), 1 Among constraint (tier-3). 25% of applications have a splitAmong constraint (two instances of the application place on distinct zones). The datacenter has 1 a SingleResourceCapacity to limit the resource provision of each node (60 ucpu, 120 GB RAM) and a MaxOnlines constraint to limit the number of online nodes to 240.

**evaluator:** performs the evaluation according the passed arguments. The evaluator creates change in datacenter's environment (workload, failure, bootstorm) and reconfigures the datacenters. Futhermore, the evaluator checks for temporary violations of the constraints and records the reconfiguration plan in an output file.


## Usage
`./instanceMaker [-r number of racks] [-p number of node per rack] [-a number of applications] -o ouput.json`
Generator an instance. The resulting JSON file is stored in `output.json`

*Example:* `instanceMaker -r 16 -p 16 -a 350 -o foo.json`
Produce an instance stored in `foo.json`. The model consists consists of 16 racks, 16 nodes per rack, and 350 Applications.

`./evaluator [-c] [-t timeout] [-o output] -i instance -a constraints -s scenario `
-c  find the reconfiguration plan with continuous restriction   
-t  solver timeout  
-o  output path for the result
-s  the reconfiguration scenario. It consists of [ve,he,sf,bs]

*Example:* `Evaluator -m model.json -a application.json -s ve -t 60 -o result`

