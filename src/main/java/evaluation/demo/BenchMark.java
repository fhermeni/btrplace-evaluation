package evaluation.demo;

import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.json.model.constraint.SatConstraintsConverter;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import evaluation.generator.CpuPeak;
import evaluation.generator.EvaluationTools;
import evaluation.generator.HardwareFailures;
import evaluation.generator.IncreasingLoad;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/22/13
 * Time: 3:41 PM
 */
public class BenchMark {

    private static boolean cont = false;
    private static String model_file = "";
    private static String output_file = "";
    private static String event = "";
    private static Set<String> constraints;
    private static int iPercent = 0;


    private enum EventType {
        load, failure, peak
    }


    public static void main(String[] args) {
        parseOptions(args);
        Model model = getModel();
        Set<SatConstraint> constraints = getConstraints();
        Model fixed_model = EvaluationTools.prepareModel(model, constraints);
        System.out.println("Current Load: " + EvaluationTools.currentLoad(fixed_model));
        if (cont) for (SatConstraint c : constraints) c.setContinuous(true);
        switch (EventType.valueOf(event)) {
            case load:
                IncreasingLoad incLoad = new IncreasingLoad(fixed_model, constraints);
                checkPlanResult(incLoad.run());
                break;

            case failure:
                HardwareFailures failures = new HardwareFailures(fixed_model, constraints);
                checkPlanResult(failures.run());
                break;

            case peak:
                CpuPeak cpuPeak = new CpuPeak(fixed_model, constraints);
                cpuPeak.setPercent(iPercent);
                checkPlanResult(cpuPeak.run());
                break;
        }

        System.exit(0);
    }

    public static Model getModel() {
        ModelConverter modelConverter = new ModelConverter();
        Model m = new DefaultModel(new DefaultMapping());
        try {
            m = modelConverter.fromJSON(new File(model_file));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(0);
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(0);
        }
        return m;
    }

    public static Set<SatConstraint> getConstraints() {
        SatConstraintsConverter satConstraintsConverter = new SatConstraintsConverter();
        Set<SatConstraint> ctrs = new HashSet<SatConstraint>();
        try {
            for (String s : constraints) {
                List<SatConstraint> satConstraint = satConstraintsConverter.listFromJSON(new File(s));
                ctrs.addAll(satConstraint);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return ctrs;
    }

    public static void recordPlan(ReconfigurationPlan plan) {
        if (plan == null) {
            System.out.println("Plan is NULL");
            System.exit(0);
        }
        ReconfigurationPlanConverter rpc = new ReconfigurationPlanConverter();
        try {
            rpc.toJSON(plan, new File(output_file));
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    private static void parseOptions(String[] args) {
        Options options = new Options();
        options.addOption("c", false, "For continuous restriction");
        options.addOption("d", false, "For discrete restriction");
        options.addOption("h", false, "For Help");
        options.addOption("m", true, "Model file");
        options.addOption("o", true, "Plan output file");
        options.addOption("e", true, "Event type: [load, failure, peak]");
        options.addOption("i", true, "Percent of load increase");

        CommandLineParser parser = new BasicParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("c")) {
                cont = true;
            }
            if (line.hasOption("o")) {
                output_file = line.getOptionValue("o");
            } else {
                output_file = (cont) ? "cplan.json" : "dplan.json";
            }
            if (line.hasOption("m")) {
                model_file = line.getOptionValue("m");
            }
            if (line.hasOption("e")) {
                event = line.getOptionValue("e");
            }
            if (line.hasOption("i")) {
                iPercent = Integer.parseInt(line.getOptionValue("i"));
            }
            constraints = new HashSet<String>(Arrays.asList(line.getArgs()));

            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("generator", options, true);
            }
        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("generator", options, true);
        }
    }

    public static void checkPlanResult(ReconfigurationPlan plan) {
        if (plan == null) {
            System.out.println("The constraints are already satisfied or BtrPlace has no solution");
            System.exit(-1);
        }
        recordPlan(plan);
        System.out.println("After Load: " + EvaluationTools.currentLoad(plan.getResult()));
        System.out.println(String.format("Plan: %d actions\t%d seconds", plan.getSize(), plan.getDuration()));
    }

    public static void recordModel(Model model) {
        ModelConverter modelConverter = new ModelConverter();
        try {
            modelConverter.toJSON(model, new File("result" + model_file));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(0);
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(0);
        }
    }
}