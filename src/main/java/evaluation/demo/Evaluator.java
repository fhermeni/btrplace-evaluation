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
        ExecutorService thread = Executors.newFixedThreadPool(4);
        ReconfigurationScenario.setTimeOut(300);
        ReconfigurationScenario.findContinuous();
        ReconfigurationScenario rs;

//        int nP = 100;
//        int[] problems = {3, 13, 14, 29, 34, 44, 52, 70, 76, 89, 95, 97};  // Vertical Elasticity
//        int[] problems = {20, 48, 70, 76};    // Horizontal Elasticity
//        int[] problems = {14, 15, 29, 43, 48, 64, 65, 91};  // Boot Storm
        int[] problems = {3, 5, 13, 14, 19, 20, 21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 36, 37, 39, 40, 44, 45, 46, 48,
                50, 52, 55, 56, 58, 58, 59, 61, 62, 63, 64, 65, 71, 72, 73, 74, 75, 76, 77, 78, 80, 82, 88, 89,
                90, 92, 93, 94, 96, 98, 99, 100};  // Server failure
//        SType[] type = {SType.ve, SType.he, SType.sf ,SType.bs};
        SType type = SType.valueOf(args[0]);

        switch (type) {
            case ve:
//                for (int i = 1; i <= nP; i++) {
                for (int i : problems) {
                    rs = new VerticalElasticity(i);
                    thread.execute(rs);
                }
                break;
            case he:
//                for (int i = 1; i <= nP; i++) {
                for (int i : problems) {
                    rs = new HorizontalElasticity(i);
                    thread.execute(rs);
                }
                break;
            case sf:
//                for (int i = 1; i <= nP; i++) {
                for (int i : problems) {
                    rs = new ServerFailures(i);
                    thread.execute(rs);
                }
                break;
            case bs:
//                for (int i = 1; i <= nP; i++) {
                for (int i : problems) {
                    rs = new BootStorm(i);
                    thread.execute(rs);
                }
                break;
        }

        thread.shutdown();
    }

    enum SType {
        ve, he, sf, bs
    }
}
