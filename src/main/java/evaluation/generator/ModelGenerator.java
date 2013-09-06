package evaluation.generator;

import btrplace.model.*;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 5/15/13
 * Time: 10:09 AM
 */
public class ModelGenerator {
    private static int NUMBER_OF_NODE;
    private static int NUMBER_OF_VM;

    public Node[] nodes;
    public VM[] vms;
    public Model pModel;
    private Random rand = new Random(System.nanoTime() % 100000);

    public ModelGenerator() {
        pModel = new DefaultModel();
    }

    public Model generateModel(int n, int vm, boolean identical) {
        Model model = new DefaultModel();
        ShareableResource cpus = new ShareableResource("cpu", 4, 1);
        ShareableResource mems = new ShareableResource("mem", 8, 1);
        NUMBER_OF_NODE = n;
        NUMBER_OF_VM = vm;
        nodes = new Node[NUMBER_OF_NODE];
        vms = new VM[NUMBER_OF_VM];
        if (!identical) {
            generateHeterogeneity(model, cpus, mems);
        }
        else generateIdentical(model, cpus, mems);

        Running run = new Running(model.getMapping().getAllVMs());
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        try {
            ReconfigurationPlan plan = cra.solve(model, new HashSet<SatConstraint>(Collections.singleton(run)));
            pModel = plan.getResult();
            return pModel;
        } catch (SolverException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public void generateHeterogeneity(Model model, ShareableResource... srcs) {
        for (int i = 0; i < NUMBER_OF_NODE; i++) {
            Node node = model.newNode();
            model.getMapping().addOnlineNode(node);
            nodes[i] = node;
            Random r = new Random();
            for (ShareableResource sr : srcs) {
                int value = (r.nextInt(4) + 1) * 4;
                sr.setCapacity(node, value);
            }
        }
        for (int i = 0; i < NUMBER_OF_VM; i++) {
            VM vm = model.newVM();
            model.getMapping().addReadyVM(vm);
            vms[i] = vm;
            Random r = new Random();
            for (ShareableResource sr : srcs) {
                int value = (r.nextInt(2) + 1);
                sr.setConsumption(vm, value);
            }
        }
        for (ShareableResource sr : srcs) {
            model.attach(sr);
        }
    }

    private void generateIdentical(Model model, ShareableResource cpus, ShareableResource mems) {
        for (int i = 0; i < NUMBER_OF_NODE; i++) {
            Node node = model.newNode();
            model.getMapping().addOnlineNode(node);
            nodes[i] = node;
        }
        for (int i = 0; i < NUMBER_OF_VM; i++) {
            VM vm = model.newVM();
            model.getMapping().addReadyVM(vm);
            vms[i] = vm;
        }

        model.attach(cpus);
        model.attach(mems);
    }


    public Set<VM> getRandomVMs(int size) {

        Set<VM> vm_set = new HashSet<VM>();
        Set<Integer> v_ids = new HashSet<Integer>(size);
        for (int i = 0; i < size; i++) {
            int randomId;
            do {
                randomId = rand.nextInt(vms.length);
            }
            while (v_ids.contains(randomId));
            v_ids.add(randomId);
            vm_set.add(vms[randomId]);
        }
        return vm_set;
    }

    public Set<VM> getSpreadVMs(int size) {
        Set<VM> vmSet = new HashSet<VM>();
        Set<Node> hostSet = new HashSet<Node>();
        VM vm;
        Node vmLocation;
        for (int i = 0; i < size; i++) {
            int randomId;
            do {
                randomId = rand.nextInt(vms.length);
                vm = vms[randomId];
                vmLocation = pModel.getMapping().getVMLocation(vm);
            }
            while (hostSet.contains(vmLocation));
            hostSet.add(vmLocation);
            vmSet.add(vm);
        }
        return vmSet;
    }

    public Set<Node> getRandomNodes(int size) {

        Set<Node> node_set = new HashSet<Node>();
        Set<Integer> node_ids = new HashSet<Integer>(size);
        for (int i = 0; i < size; i++) {
            int randomId;
            do {
                randomId = rand.nextInt(nodes.length);
            }
            while (node_ids.contains(randomId));
            node_ids.add(randomId);
            node_set.add(nodes[randomId]);
        }
        return node_set;
    }

    public Collection<Collection<Node>> getDistinctSet(int Number_of_Set) {
        Collection<Collection<Node>> collection = new HashSet<Collection<Node>>();
        int j = 0;
        for (int i = 0; i < Number_of_Set; i++) {
            Set<Node> tmpSet = new HashSet<Node>();
            int size = NUMBER_OF_NODE / Number_of_Set;
            for (int k = 0; k < size; k++) {
                tmpSet.add(nodes[j++]);
            }
            collection.add(tmpSet);
        }
        return collection;
    }


}
