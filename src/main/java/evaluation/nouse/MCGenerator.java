package evaluation.nouse;

import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.json.model.constraint.SatConstraintsConverter;
import btrplace.model.Model;
import btrplace.model.constraint.*;
import evaluation.generator.ModelGenerator;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/28/13
 * Time: 3:38 PM
 */
public class MCGenerator {

    private static boolean continuous;
    private static boolean identical;
    private static String constraint_name;
    private static int nNode;
    private static int nVM;
    private static int vmsSize;
    private static int nodeSize;

    private enum Constraint {
        among, ban, CReC, CVmC, fence, gather, killed, lonely, offline, online, overbook, preserve, quarantine,
        ready, root, running, SVMT, SReC, SVmC, split, splitAmong, spread
    }

    public static void main(String[] args) {
        parseOptions(args);
        generateConstraint(Constraint.valueOf(constraint_name));
    }

    private static void parseOptions(String[] args) {
        Options options = new Options();
        options.addOption("c", false, "For continuous restriction");
        options.addOption("i", false, "Nodes are identical");
        options.addOption("n", true, "Number of nodes");
        options.addOption("m", true, "Number of VMs");
        options.addOption("t", true, "Constraint Name");
        options.addOption("s", true, "Size of the VM set");
        options.addOption("z", true, "Size of the Node set");
        CommandLineParser parser = new BasicParser();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("c")) {
                continuous = true;
            }

            if (line.hasOption("i")) {
                identical = true;
            }

            if (line.hasOption("n")) {
                nNode = Integer.parseInt(line.getOptionValue("n"));
            }

            if (line.hasOption("m")) {
                nVM = Integer.parseInt(line.getOptionValue("m"));
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

        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("generator", options, true);
            System.exit(-1);
        }
    }

    public static void generateConstraint(Constraint name) {
        Set<SatConstraint> constraintSet = new HashSet<SatConstraint>();
        ModelGenerator gen = new ModelGenerator();
        Model model = gen.generateModel(nNode, nVM, identical);
        switch (name) {
            case among:
                Among among = new Among(gen.getRandomVMs(vmsSize), gen.getDistinctSet(nodeSize), continuous);

                constraintSet.add(among);
                break;

            case spread:
                Spread spread = new Spread(gen.getRandomVMs(vmsSize), continuous);
                constraintSet.add(spread);
                break;

            case ban:
                Ban ban = new Ban(gen.getRandomVMs(vmsSize), gen.getRandomNodes(nodeSize));
                constraintSet.add(ban);
                break;

            case fence:
                Fence fence = new Fence(gen.getRandomVMs(vmsSize), gen.getRandomNodes(nodeSize));
                constraintSet.add(fence);
                break;

            case lonely:
                Lonely lonely = new Lonely(gen.getRandomVMs(vmsSize), continuous);
                constraintSet.add(lonely);
                break;

            case quarantine:
                Quarantine quarantine = new Quarantine(gen.getRandomNodes(vmsSize));
                constraintSet.add(quarantine);
                break;
        }
        System.out.println(model.getMapping());
        recordModel(model);
        recordConstraints(constraintSet);
    }

    public static void recordConstraints(Set<SatConstraint> constraints) {
        SatConstraintsConverter converter = new SatConstraintsConverter();
        try {
            converter.toJSON(constraints, new File(constraint_name + "Constraint.json"));

        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static void recordModel(Model model) {
        ModelConverter converter = new ModelConverter();
        try {
            converter.toJSON(model, new File(constraint_name + "Model.json"));
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
