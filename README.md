tion of continuous satisfaction of the constraints in BtrPlace

## Introduction

[BtrPlace](http://btrp.inria.fr) is a virtual machines placement algorithm for hosting platforms. It can be customized by administrators to ensure the satisfaction of SLAs and to perform the administrative management operations.

In this project, we evaluate the level of satisfaction of the constraint during the reconfiguration of datacenters due to the hardware failures or increasing in the workload of VMs.


#Evaluation Protocol
To evaluation Btrplace, We use the specification of the [Open Cirrus Cloud Computing Testbed](http://opencirrus.org) and the [Open Cloud Testbed](http://opencloudconsortium.org). Relying on the detail specification of Open  Cirrus, we simulate the datacenters as follow:    
**Open Cirrus** is a federated heterogeneous distributed data centers. It consists of several different geographic sites, each has 1000+ cores. Below is the data from [Open Cirrus: A global cloud computing testbed](http://www.cs.cmu.edu/~droh/papers/opencirrus-ieeecomputer.pdf)
<table>
  <tr>
    <th>Site</th>
    <th>#Cores</th>
    <th>#Servers</th>
    <th>Disk Size</th>
  </tr>
  <tr>
    <td>HP</td>
    <td>1024</td>
    <td>256</td>
    <td>3.3TB</td>
  </tr>
  <tr>
    <td>IDA</td>
    <td>2400</td>
    <td>300</td>
    <td>4.8TB</td>
  </tr>
  <tr>
    <td>Intel</td>
    <td>1364</td>
    <td>198</td>
    <td>1.77TB</td>
  </tr>
  <tr>
    <td>KIT</td>
    <td>2656</td>
    <td>232</td>
    <td>10TB</td>
  </tr>
  <tr>
    <td>UIUC</td>
    <td>1024</td>
    <td>128</td>
    <td>2TB</td>
  </tr>
  <tr>
    <td>Yahoo</td>
    <td>3200</td>
    <td>480</td>
    <td>2.4TB</td>
  </tr>
</table>

**Open Cloud Testbed** has 120 commondity nodes in four data centers which are connected with a high performance 10Gb/s network. The specification of each node is shown below:
<table>
  <tr>
    <th>#Cores</th>
    <th>RAM</th>
    <th>Disk Size</th>
  </tr>
  <tr>
    <td>4</td>
    <td>12GB</td>
    <td>1TB</td>
</table>

## Tools

The evaluation contain:  
* Model & Constraints Generator  
* Benchmark  
* PlanChecker  

**MCGenerator:**  Generates a model and a constraint associates with the model. One can specify the number of nodes and VMs in the model. Furthermore, for some constraints need a set of VM or a set of Node, this can be done by passing numbers in command's parameters.

**Benchmark:** fixes the model if it doesn't satisfy the constraint, then the benchmark creates the increase in workload of VM by add the Preserve constraints on the set of VMs invloved in the tested constraints.

**PlanChecker:** Use to check whether the plan computed in the discrete satisfaction of the constraints satisfies their continuous satisfaction.

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

