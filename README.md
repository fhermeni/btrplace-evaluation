## Introduction

[BtrPlace](http://btrp.inria.fr) is a virtual machines placement algorithm for hosting platforms. It can be customized
by administrators to ensure the satisfaction of SLAs and to perform the administrative management operations.

In this project, we evaluate the level of satisfaction of the constraint during the reconfiguration of datacenters due
to the hardware failures or increasing in the workload of VMs.


#Evaluation Protocol
To evaluation Btrplace, We use the specification of the [Dell Active System 50](http://www.dell.com/us/business/p/dell-vstart-50/pd), a pre-built managed converged system wfor small datacenter. The worknode specification details are listed below.  
Dell PowerEdge R620 Server: 2S/1U, 8 cores/socket, 2 threads / core.  64GB of memory.   
Storage: Local Hard Drive: 300GB. Storage array: 7.2TB  
**Server Summary**
<table>
  <tr>
    <th>#Socket</th>
    <th>#Cores</th>
    <th>#Threads</th>
    <th>RAM</th>
    <th>Disk Size</th>
  </tr>
  <tr>
    <td>2</td>
  <td>8</td>
	<td>16</td>
    <td>64GB</td>
    <td>320TB</td>
</table>

**Datacenter configuration**   
A medium datacenter, e.g [Open Cloud Testbed](http://opencloudconsortium.org), consists of 120 working nodes racked in 4 racks each mounted 30 nodes. 
In this datacenter, we run many n-tiers applications that have multiple replicas for each tier to ensure high availability,
fault tolerance and high performance properties. For instance, a 3-tier application has 2 VMs for the Presentation Layer,
4 VMs for Business Logic and Data Layers.


**Datacenter consolidation scenarios**  

1. Vertical Scaling  
We simulate the increasing load of the applications by preserving more CPU/RAM for some arbitrary VMs until it
causes the consolidation happens. 

2. Horizontal Scaling  
Application's workload increases, more VMs needed for each tier to handle the increasing load. The set of constraints of the application then include
the cloned VMs. 

3. Hardware Failure / Network Maintenance  
The average rate of hardware failure in a datacenter is around 5% at any givent moment. The network maintenance operation also brings down some nodes.
We simulate this event by turning off random nodes in the datacenter. 

4. BootStorm VM  
There are some moments during a working day, hundreds of VMs are powered on / off simutaneously (the beginning and the end of a working day). It may 
affect the performance of the applications. The consolidation manager need to reconfigure the datacenter for load balancing and satisfaction of SLAs.

**Evaluation**
In each scenario, we record the reconfiguration plans computed by BtrPlace both for discrete restriction
and continuous restriction of the constraints. After that, we compare the time needed to compute the plans, and to
complete the reconfiguration process. Additionally, we compare the number of actions and the dependency between the action
specified in the plans.

## Tools

The evaluation contain:  
* Model & Constraints Generator  
* Benchmark  
* PlanChecker  

**MCGenerator:**  Generates a model and a constraint associates with the model. One can specify the number of nodes and
 VMs in the model. Furthermore, for some constraints need a set of VM or a set of Node, this can be done by passing
 numbers in command's parameters.

**Benchmark:** fixes the model if it doesn't satisfy the constraint, then the benchmark creates the increase in workload
 of VM by add the Preserve constraints on the set of VMs invloved in the tested constraints.

**PlanChecker:** Use to check whether the plan computed in the discrete satisfaction of the constraints satisfies their
continuous satisfaction.

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

