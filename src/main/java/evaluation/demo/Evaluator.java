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
        String out = null;
        String in = null;
        Options options = new Options();
        Option o = new Option("s", true, "Reconfiguration Scenario");
        o.setRequired(true);
        options.addOption(o);
        options.addOption("t", true, "Solver timeout");
        options.addOption("c", false, "Continuous restriction");
        o = new Option("i", true, "instance");
        o.setRequired(true);
        options.addOption(o);
        options.addOption("o", true, "Output path for result");

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
            if (line.hasOption("o")) {
                out = line.getOptionValue("o");
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(Evaluator.class.getSimpleName(), options);
            System.exit(1);
        }


        ReconfigurationScenario.setTimeOut(timeout);
        if (cont) ReconfigurationScenario.findContinuous();
        ReconfigurationScenario rs;

        try {
        switch (type) {
            case ve:
                rs = new VerticalElasticity(in, out);
                rs.run();
                output.append(rs);
                break;
            case he:
                rs = new HorizontalElasticity(in, out);
                rs.run();
                output.append(rs);
                break;
            case sf:
                rs = new ServerFailures(in, out);
                rs.run();
                output.append(rs);
                break;
            case bs:
                rs = new BootStorm(in, out);
                rs.run();
                output.append(rs);
                break;
        }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (out != null) {
            try {
                FileWriter toFile = new FileWriter(out + ".txt");
                toFile.write(output.toString());
                toFile.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    enum SType {
        ve, he, sf, bs
    }
}
