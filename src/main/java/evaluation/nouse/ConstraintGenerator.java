package evaluation.nouse;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.*;
import evaluation.generator.ConverterTools;
import org.apache.commons.cli.*;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: TU HUYNH DANG
 * Date: 6/3/13
 * Time: 4:13 PM
 */
public class ConstraintGenerator {

    public static Model model;
    boolean continuous;
    String constraint_name;
    String model_file;
    int vmsSize;
    int nodeSize;
    int number;
    Random rand = new Random(System.nanoTime() % 100000);

    public static void main(String[] args) {
        ConstraintGenerator cg = new ConstraintGenerator();
        cg.parseOptions(args);
        Model model = ConverterTools.getModelFromFile(cg.model_file);
        Collection<SatConstraint> collection = cg.generateConstraints(model, Constraint.valueOf(cg.constraint_name), cg.number);
        String name = (cg.continuous) ? "C" : "D";
        ConverterTools.constraintsToFile(collection, name + cg.constraint_name);
        System.out.println(String.format("Generate %d %s constraints successfully", cg.number, cg.constraint_name));

    }

    public Collection<SatConstraint> generateConstraints(Model model, Constraint name, int number) {
        Collection<SatConstraint> collection = new ArrayList<SatConstraint>();
        while (number > 0) {
            collection.add(generateConstraint(model, name));
            number--;
        }
        return collection;
    }

    public SatConstraint generateConstraint(Model m, Constraint name) {
        switch (name) {
            case among:
                return new Among(getRandomVMs(m, vmsSize), getDistinctSet(model, nodeSize), continuous);

            case spread:
                return new Spread(getRandomVMs(m, vmsSize), continuous);

            case ban:
                return new Ban(getRandomVMs(m, vmsSize), getRandomNodes(m, nodeSize));

            case fence:
                return new Fence(getRandomVMs(m, vmsSize), getRandomNodes(m, nodeSize));

            case lonely:
                return new Lonely(getRandomVMs(m, vmsSize), continuous);

            case quarantine:
                return new Quarantine(getRandomNodes(m, vmsSize));
        }
        return null;
    }

    private Set<VM> getRandomVMs(Model model, int size) {
        Set<VM> vm_set = new HashSet<VM>();
        Set<Integer> v_ids = new HashSet<Integer>(size);
        ArrayList<VM> vms = new ArrayList<VM>(model.getMapping().getAllVMs());
        for (int i = 0; i < size; i++) {
            int randomId;
            do {
                randomId = rand.nextInt(vms.size());
            }
            while (v_ids.contains(randomId));
            v_ids.add(randomId);
            vm_set.add(vms.get(randomId));
        }
        return vm_set;
    }

    public Set<Node> getRandomNodes(Model model, int size) {

        Set<Node> node_set = new HashSet<Node>();
        Set<Integer> node_ids = new HashSet<Integer>(size);
        ArrayList<Node> nodes = new ArrayList<Node>(model.getMapping().getAllNodes());

        for (int i = 0; i < size; i++) {
            int randomId;
            do {
                randomId = rand.nextInt(nodes.size());
            }
            while (node_ids.contains(randomId));
            node_ids.add(randomId);
            node_set.add(nodes.get(randomId));
        }
        return node_set;
    }

    public Collection<Collection<Node>> getDistinctSet(Model model, int Number_of_Set) {
        Collection<Collection<Node>> collection = new HashSet<Collection<Node>>();
        ArrayList<Node> nodes = new ArrayList<Node>(model.getMapping().getAllNodes());
        int j = 0;
        for (int i = 0; i < Number_of_Set; i++) {
            Set<Node> tmpSet = new HashSet<Node>();
            int size = model.getNodes().size() / Number_of_Set;
            for (int k = 0; k < size; k++) {
                tmpSet.add(nodes.get(j++));
            }
            collection.add(tmpSet);
        }
        return collection;
    }

    private void parseOptions(String[] args) {
        Options options = new Options();
        options.addOption("c", false, "For continuous restriction");
        options.addOption("m", true, "Model file");
        options.addOption("t", true, "Constraint Name");
        options.addOption("n", true, "Number of constraints to create");
        options.addOption("s", true, "Size of the VM set");
        options.addOption("z", true, "Size of the Node set");
        CommandLineParser parser = new BasicParser();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("c")) {
                continuous = true;
            }

            if (line.hasOption("m")) {
                model_file = line.getOptionValue("m");
            }


            if (line.hasOption("s")) {
                vmsSize = Integer.parseInt(line.getOptionValue("s"));
            }

            if (line.hasOption("z")) {
                nodeSize = Integer.parseInt(line.getOptionValue("z"));
            }

            if (line.hasOption("t")) {
                constraint_name = line.getOptionValue("t");
            }

            if (line.hasOption("n")) {
                number = Integer.parseInt(line.getOptionValue("n"));
            }

        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("generator", options, true);
            System.exit(-1);
        }
    }


    private enum Constraint {
        among, ban, CReC, CVmC, fence, gather, killed, lonely, offline, online, overbook, preserve, quarantine,
        ready, root, running, SVMT, SReC, SVmC, split, splitAmong, spread
    }
}
