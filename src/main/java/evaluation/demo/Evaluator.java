package evaluation.demo;

import evaluation.scenarios.*;

import java.io.FileWriter;
import java.io.IOException;

/**
 * User: TU HUYNH DANG
 * Date: 6/20/13
 * Time: 10:53 AM
 */
public class Evaluator {
    public static void main(String[] args) {
        SType type = SType.valueOf(args[0]);

        StringBuilder output = new StringBuilder();

        int timeout = Integer.parseInt(args[1]);
        ReconfigurationScenario.setTimeOut(timeout);
        boolean cont = Boolean.parseBoolean(args[2]);
        if (cont) ReconfigurationScenario.findContinuous();
        ReconfigurationScenario rs;

        String filename = String.format("%s%d%b", type, timeout, cont);
//        int[] problems = {3, 13, 14, 29, 34, 44, 52, 70, 76, 89, 95, 97};  // failed cases
        int n = 100;
        int[] problems = new int[n];
        for (int i = 0; i < n; i++) {
            problems[i] = i + 1;
        }
        for (int i : problems) {
            switch (type) {
                case ve:
                    rs = new VerticalElasticity(i);
                    rs.run();
                    output.append(rs.toString());
                    break;
                case he:
                    rs = new HorizontalElasticity(i);
                    rs.run();
                    output.append(rs.toString());
                    break;
                case sf:
                    rs = new ServerFailures(i);
                    rs.run();
                    output.append(rs.toString());
                    break;
                case bs:
                    rs = new BootStorm(i);
                    rs.run();
                    output.append(rs.toString());
                    break;
                case cv:
                    rs = new ConstraintViolation(i);
                    rs.run();
                    output.append(rs.toString());
                    break;
            }
        }

        try {
            FileWriter toFile = new FileWriter(filename + ".csv");
            toFile.write(output.toString());
            toFile.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    enum SType {
        ve, he, sf, bs, cv
    }
}
