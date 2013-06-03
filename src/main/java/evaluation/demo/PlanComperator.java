package evaluation.demo;

import btrplace.json.JSONConverterException;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.plan.ReconfigurationPlan;
import evaluation.generator.EvaluationTools;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;

/**
 * User: TU HUYNH DANG
 * Date: 6/3/13
 * Time: 3:41 PM
 */
public class PlanComperator {

    ReconfigurationPlan dplan;
    ReconfigurationPlan cplan;

    public static void main(String[] args) {
        PlanComperator pc = new PlanComperator();
        pc.parseOptions(args);
        System.out.println(EvaluationTools.analyze(pc.dplan, pc.cplan));

    }


    private void parseOptions(String[] args) {
        Options options = new Options();
        options.addOption("c", true, "continuous plan");
        options.addOption("d", true, "For discrete plan");

        CommandLineParser parser = new BasicParser();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("c")) {
                cplan = parse(line.getOptionValue("c"));
            }

            if (line.hasOption("d")) {
                dplan = parse(line.getOptionValue("d"));
            }

        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("generator", options, true);
        }
    }

    private ReconfigurationPlan parse(String s) {
        ReconfigurationPlanConverter converter = new ReconfigurationPlanConverter();
        try {
            return converter.fromJSON(new File(s));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONConverterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
