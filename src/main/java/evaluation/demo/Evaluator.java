package evaluation.demo;

import evaluation.scenarios.*;
import org.apache.commons.cli.*;

import java.io.FileWriter;
import java.io.IOException;

/**
 * User: TU HUYNH DANG
 * Date: 6/20/13
 * Time: 10:53 AM
 */
public class Evaluator {
    public static void main(String... args) {
        SType type = SType.he;
        StringBuilder output = new StringBuilder();
        int timeout = 0;
        boolean cont = false;
        String res = null;
        String in = null;
        String plan = null;
        Options options = new Options();
        Option o = new Option("s", true, "Reconfiguration Scenario (he, ve, sf, bs)");
        o.setRequired(true);
        options.addOption(o);
        options.addOption("t", true, "Solver timeout in seconds");
        options.addOption("c", false, "Continuous restriction");
        o = new Option("i", true, "instance");
        o.setRequired(true);
        options.addOption(o);
        options.addOption("r", true, "output file for the results");
        options.addOption("p", true, "output JSON file for the resulting plan");

        CommandLineParser parser = new BasicParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("c")) {
                cont = true;
            }
            if (line.hasOption("s")) {
                type = SType.valueOf(line.getOptionValue("s"));
            }
            if (line.hasOption("t")) {
                timeout = Integer.parseInt(line.getOptionValue("t"));
            }
            if (line.hasOption("i")) {
                in = line.getOptionValue("i");
            }
            if (line.hasOption("r")) {
                res = line.getOptionValue("r");
            }
            if (line.hasOption("p")) {
                plan = line.getOptionValue("p");
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(Evaluator.class.getSimpleName(), options);
            System.exit(1);
        }

        ReconfigurationScenario rs = null;

        try {
        switch (type) {
            case ve:
                rs = new VerticalElasticity(in, plan);
                break;
            case he:
                rs = new HorizontalElasticity(in, plan);
                break;
            case sf:
                rs = new ServerFailures(in, plan);
                break;
            case bs:
                rs = new BootStorm(in, plan);
                break;
        }
        rs.setTimeOut(timeout);
        if (cont) rs.findContinuous();
        rs.run();
        output.append(rs);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (res != null) {
            try {
                FileWriter toFile = new FileWriter(res, true);
                toFile.write(output.toString());
                toFile.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } else {
            System.out.print(rs.toString());
        }
    }

    enum SType {
        ve, he, sf, bs
    }
}
