package evaluation.generator;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.view.ShareableResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * User: TU HUYNH DANG
 * Date: 5/15/13
 * Time: 10:09 AM
 */
public class EvModel {
    // Number of node in each cluster
    public static int NUMBER_OF_NODE = 32;
    public static Random rand = new Random(System.nanoTime() % 100000);
    public final String CPU = "cpu";
    public final String MEM = "mem";
    private ArrayList<Collection<Node>> dc;
    private Model model;

    public EvModel() {
        model = new DefaultModel();
    }

    public Collection<Collection<Node>> getdc() {
        return dc;
    }

    public Model getModel() {
        return model;
    }

    public Model generateModel() {
        Model model = getModel();

        ShareableResource cpus = new ShareableResource(CPU, 128, 1);
        ShareableResource mems = new ShareableResource(MEM, 256, 1);
        dc = new ArrayList<Collection<Node>>();
        // There are for clusters
        model.attach(cpus);
        model.attach(mems);
        for (int i = 0; i < 4; i++) {
            ArrayList<Node> nodeList = new ArrayList<Node>();
            for (int j = 0; j < NUMBER_OF_NODE; j++) {
                Node node = model.newNode();
                model.getMapping().addOnlineNode(node);
                nodeList.add(node);
            }
            dc.add(nodeList);
        }
        return model;
    }

    public Collection<Node> getCluster(int idx) {
        return dc.get(idx);
    }

    public boolean runApp(String type) {
        Model model = getModel();
        VM vm = model.newVM();
        model.getMapping().addReadyVM(vm);
        ShareableResource cpu = (ShareableResource) model.getView(CPU);
        final ShareableResource mem = (ShareableResource) model.getView(MEM);
        switch (AppType.valueOf(type)) {
            case computeXL:
                cpu.setConsumption(vm, 2);
                mem.setConsumption(vm, 2);
                break;
            case compute8XL:
                cpu.setConsumption(vm, 8);
                mem.setConsumption(vm, 15);
                break;
            case memoryXL:
                cpu.setConsumption(vm, 1);
                mem.setConsumption(vm, 5);
                break;
            case memory2XL:
                cpu.setConsumption(vm, 1);
                mem.setConsumption(vm, 9);
                break;
            case memory4XL:
                cpu.setConsumption(vm, 2);
                mem.setConsumption(vm, 17);
            default:
                cpu.setConsumption(vm, 1);
                mem.setConsumption(vm, 1);
        }
        return true;
    }

    enum AppType {
        generalS, generalM, generalL, computeM, computeXL, compute8XL, memoryXL, memory2XL, memory4XL
    }

}
