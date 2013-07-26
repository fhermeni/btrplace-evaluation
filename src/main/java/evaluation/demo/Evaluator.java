package evaluation.demo;

import evaluation.scenarios.*;
import org.apache.commons.cli.*;

import java.io.File;
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
        String mfile = null;
        String appFile = null;
        String out = null;
        Options options = new Options();
        options.addOption("s", true, "Reconfiguration Scenario");
        options.addOption("t", true, "Solver timeout");
        options.addOption("c", false, "Continuous restriction");
        options.addOption("m", true, "Input model");
        options.addOption("a", true, "Input application constraints");
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
            if (line.hasOption("m")) {
                mfile = line.getOptionValue("m");
            }
            if (line.hasOption("a")) {
                appFile = line.getOptionValue("a");
            }
            if (line.hasOption("o")) {
                out = line.getOptionValue("o");
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Evaluator", options);
            System.exit(0);
        }


        ReconfigurationScenario.setTimeOut(timeout);
        if (cont) ReconfigurationScenario.findContinuous();
        ReconfigurationScenario rs;

        switch (type) {
            case ve:
                rs = new VerticalElasticity(mfile, appFile, out);
                rs.run();
                output.append(rs.toString());
                break;
            case he:
                rs = new HorizontalElasticity(mfile, appFile, out);
                rs.run();
                output.append(rs.toString());
                break;
            case sf:
                rs = new ServerFailures(mfile, appFile, out);
                rs.run();
                output.append(rs.toString());
                break;
            case bs:
                rs = new BootStorm(mfile, appFile, out);
                rs.run();
                output.append(rs.toString());
                break;
        }
        if (out != null) {
            String filename = String.format("%s%s%d%b", new File(mfile).getName(), type, timeout, cont);
            try {
                FileWriter toFile = new FileWriter(out + filename + ".txt");
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
