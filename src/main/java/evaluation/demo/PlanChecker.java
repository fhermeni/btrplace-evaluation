package evaluation.demo;

import btrplace.json.JSONConverterException;
import btrplace.json.model.constraint.SatConstraintsConverter;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import evaluation.generator.EvaluationTools;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/23/13
 * Time: 12:38 PM
 */
public class PlanChecker {

    private static boolean cont;
    private static HashSet<String> constraints;
    private static String plan_file;

    public static void main(String[] args) {
        parseOptions(args);

        ReconfigurationPlanConverter planConverter = new ReconfigurationPlanConverter();
        SatConstraintsConverter satConstraintsConverter = new SatConstraintsConverter();
        try {
            Set<SatConstraint> dConstr = new HashSet<SatConstraint>();
            ReconfigurationPlan plan = planConverter.fromJSON(new File(plan_file));
            for (String s : constraints) {
                List<SatConstraint> satConstraint = satConstraintsConverter.listFromJSON(new File(s));
                dConstr.addAll(new HashSet<SatConstraint>(satConstraint));

            }
            if (!cont) {
                dConstr = EvaluationTools.toDiscrete(dConstr);
            }
            SatConstraint disSatisfied = check(plan, dConstr);
            if (disSatisfied == null) {
                System.out.println("The plan satisfies all the constraints");
            } else {
                System.out.println(String.format("The plan does NOT satisfy the constraint:\n%s", disSatisfied));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }


    static public SatConstraint check(ReconfigurationPlan plan, Set<SatConstraint> co) {
        if (plan == null) {
            System.err.println("No plan");
            System.exit(-1);
        }

        for (SatConstraint c : co) {
            if (c.isContinuous()) {
                if (!c.isSatisfied(plan)) {
                    return c;
                }
            } else {
                if (!c.isSatisfied(plan.getResult())) {
                    return c;
                }
            }
        }
        return null;
    }

    private static void parseOptions(String[] args) {
        Options options = new Options();
        options.addOption("c", false, "For continuous restriction");
        options.addOption("d", false, "For discrete restriction");
        options.addOption("p", true, "Plan file");

        CommandLineParser parser = new BasicParser();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("c")) {
                cont = true;
            }

            if (line.hasOption("d")) {
                cont = false;
            }

            if (line.hasOption("p")) {
                plan_file = line.getOptionValue("p");
            }


            constraints = new HashSet<String>();
            for (String s : line.getArgs()) {
                constraints.add(s);
            }

        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("generator", options, true);
        }
    }

}
