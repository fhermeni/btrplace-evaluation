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
        ExecutorService thread = Executors.newFixedThreadPool(2);
        ReconfigurationScenario.setTimeOut(20);  // 12 hours for Server Failure scenario
        ReconfigurationScenario.findContinuous();
        ReconfigurationScenario rs;

        int nP = 4;
        SType[] type = {SType.ve, SType.he, SType.sf ,SType.bs};

        for (int j = 0; j < 4; j++) {
            switch (type[j]) {
                case ve:
                    for (int i = 1; i <= nP; i++) {
                        rs = new VerticalElasticity(i);
                        thread.execute(rs);
                    }
                    break;
                case he:
                    for (int i = 1; i <= nP; i++) {
                        rs = new HorizontalElasticity(i);
                        thread.execute(rs);
                    }
                    break;
                case sf:
                    for (int i = 1; i <= nP; i++) {
                        rs = new ServerFailures(i);
                        thread.execute(rs);
                    }
                    break;
                case bs:
                    for (int i = 1; i <= nP; i++) {
                        rs = new BootStorm(i);
                        thread.execute(rs);
                    }
                    break;
            }
        }

        thread.shutdown();
    }

    enum SType {
        ve, he, sf, bs
    }
}
