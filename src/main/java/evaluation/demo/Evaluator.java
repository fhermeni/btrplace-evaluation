package evaluation.demo;

import evaluation.scenarios.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: TU HUYNH DANG
 * Date: 6/20/13
 * Time: 10:53 AM
 */
public class Evaluator {
    public static void main(String[] args) {
        SType type = SType.valueOf(args[0]);
        ReconfigurationScenario.setTimeOut(Integer.parseInt(args[1]));
        ExecutorService thread = Executors.newFixedThreadPool(Integer.parseInt(args[2]));

        if (Boolean.parseBoolean(args[3])) ReconfigurationScenario.findContinuous();

        ReconfigurationScenario rs;

        int nP = 100;
//        int[] problems = {3, 13, 14, 29, 34, 44, 52, 70, 76, 89, 95, 97};  // Vertical Elasticity
//        int[] problems = {3, 5, 13, 14, 19, 20, 21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 36, 37, 39, 40, 44, 45, 46, 48,
//                50, 52, 55, 56, 58, 58, 59, 61, 62, 63, 64, 65, 71, 72, 73, 74, 75, 76, 77, 78, 80, 82, 88, 89,
//                90, 92, 93, 94, 96, 98, 99, 100};  // Server failure
        for (int i = 1; i <= nP; i++) {
            switch (type) {
                case ve:
                    rs = new VerticalElasticity(i);
                    thread.execute(rs);
                    break;
                case he:
                    rs = new HorizontalElasticity(i);
                    thread.execute(rs);
                    break;
                case sf:
                    rs = new ServerFailures(i);
                    thread.execute(rs);
                    break;
                case bs:
                    rs = new BootStorm(i);
                    thread.execute(rs);
                    break;
                case cv:
                    rs = new ConstraintViolation(i);
                    thread.execute(rs);
                    break;
            }
        }
        thread.shutdown();
    }

    enum SType {
        ve, he, sf, bs, cv
    }
}
