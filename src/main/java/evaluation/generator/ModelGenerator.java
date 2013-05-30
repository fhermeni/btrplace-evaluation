package evaluation.generator;

import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.view.ShareableResource;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/15/13
 * Time: 10:09 AM
 */
public class ModelGenerator {
    private static int NUMBER_OF_NODE;
    private static int NUMBER_OF_VM;

    public UUID[] nodes = new UUID[NUMBER_OF_NODE];
    public UUID[] vms = new UUID[NUMBER_OF_VM];
    public Model model;
    public Mapping mapping;
    public ShareableResource cpus;
    public ShareableResource mems;
    private Random rand = new Random(System.nanoTime() % 100000);

    public ModelGenerator() {

        cpus = new ShareableResource("cpu", 1);
        mems = new ShareableResource("mem", 1);
        mapping = new DefaultMapping();
    }

    public Model generateModel(int n, int vm) {
        NUMBER_OF_NODE = n;
        NUMBER_OF_VM = vm;
        nodes = new UUID[NUMBER_OF_NODE];
        vms = new UUID[NUMBER_OF_VM];

        int vmd[] = {1, 2, 4};
        int node_cpu_capa[] = {6, 12, 8, 16, 10, 20};
        int node_mem_capa[] = {8, 16, 16, 24, 32, 64};

        for (int i = 0; i < NUMBER_OF_NODE; i++) {
            UUID uuid = new UUID(1, i);
            nodes[i] = uuid;
            cpus.set(uuid, node_cpu_capa[i % node_cpu_capa.length]);
            mems.set(uuid, node_mem_capa[i % node_mem_capa.length]);
        }

        for (int i = 0; i < NUMBER_OF_VM; i++) {
            UUID uuid = new UUID(0, i);
            vms[i] = uuid;
            Random r = new Random();
            cpus.set(uuid, vmd[i % vmd.length]);
            mems.set(uuid, vmd[i % vmd.length] * (r.nextInt(2) + 1));
        }
        mapping = generateMapping();
        model = new DefaultModel(mapping);
        model.attach(cpus);
        model.attach(mems);
        return model;
    }

    public Model generateModel(int n, int vm, boolean heterogeneous) {
        if (heterogeneous) {
            return generateModel(n, vm);
        } else {
            NUMBER_OF_NODE = n;
            NUMBER_OF_VM = vm;
            nodes = new UUID[NUMBER_OF_NODE];
            vms = new UUID[NUMBER_OF_VM];

            for (int i = 0; i < NUMBER_OF_NODE; i++) {
                UUID uuid = new UUID(1, i);
                nodes[i] = uuid;
                cpus.set(uuid, 4);
                mems.set(uuid, 8);
            }

            for (int i = 0; i < NUMBER_OF_VM; i++) {
                UUID uuid = new UUID(0, i);
                vms[i] = uuid;
                cpus.set(uuid, 1);
                mems.set(uuid, 1);
            }
            mapping = generateMapping();
            model = new DefaultModel(mapping);
            model.attach(cpus);
            model.attach(mems);
            return model;
        }
    }


    private Mapping generateMapping() {
        Mapping map = new DefaultMapping();
        int[] current_cpu_cap = new int[NUMBER_OF_NODE];
        int[] current_mem_cap = new int[NUMBER_OF_NODE];

        for (int i = 0; i < NUMBER_OF_NODE; i++) {
            current_cpu_cap[i] = cpus.get(nodes[i]);
            current_mem_cap[i] = mems.get(nodes[i]);
            map.addOnlineNode(nodes[i]);
        }
        for (UUID vm : vms) {
            int dc = cpus.get(vm);
            int dm = mems.get(vm);
            Random r = new Random();
            boolean not_place = true;
            while (not_place) {
                int index = r.nextInt(NUMBER_OF_NODE);
                if (dc <= current_cpu_cap[index] && dm <= current_mem_cap[index]) {
                    current_cpu_cap[index] -= dc;
                    current_mem_cap[index] -= dm;
                    map.addRunningVM(vm, nodes[index]);
                    not_place = false;
                }
            }
        }
        return map;
    }

    public Set<UUID> getRandomVMs(int size) {

        Set<UUID> vm_set = new HashSet<UUID>();
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

    public Set<UUID> getSpreadVMs(int size) {
        Set<UUID> vmSet = new HashSet<UUID>();
        Set<UUID> hostSet = new HashSet<UUID>();
        UUID vm;
        UUID vmLocation;
        for (int i = 0; i < size; i++) {
            int randomId;
            do {
                randomId = rand.nextInt(vms.length);
                vm = vms[randomId];
                vmLocation = mapping.getVMLocation(vm);
            }
            while (hostSet.contains(vmLocation));
            hostSet.add(vmLocation);
            vmSet.add(vm);
        }
        return vmSet;
    }

    public Set<UUID> getRandomNodes(int size) {

        Set<UUID> node_set = new HashSet<UUID>();
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

    public Set<Set<UUID>> getDistinctSet(int Number_of_Set) {
        Set<Set<UUID>> collection = new HashSet<Set<UUID>>();
        int j = 0;
        for (int i = 0; i < Number_of_Set; i++) {
            Set<UUID> tmpSet = new HashSet<UUID>();
            int size = NUMBER_OF_NODE / Number_of_Set;
            for (int k = 0; k < size; k++) {
                tmpSet.add(nodes[j++]);
            }
            collection.add(tmpSet);
        }
        return collection;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s\n%s\n%s\n", mapping.toString(), cpus.toString(), mems.toString()));
        return sb.toString();
    }

    public static void main(String[] args) {
        ModelGenerator mg = new ModelGenerator();
        Model model1 = mg.generateModel(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        ModelConverter converter = new ModelConverter();
        try {
            converter.toJSON(model1, new File(String.format("%sNode%sVM", args[0], args[1])));
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
