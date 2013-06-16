package evaluation.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: Tu Huynh Dang
 * Date: 6/16/13
 * Time: 1:04 PM
 */
public class Driver {

    public static void main(String[] args) {
        ExecutorService thread = Executors.newFixedThreadPool(4);
        /*int nP = 4;

        for (int i = 0; i < nP; i++) {
            VerticalElasticity among = new VerticalElasticity(ReconfigurationScenario.EvaluateConstraint.among);
            thread.execute(among);
        }
        */
        VerticalElasticity spread = new VerticalElasticity(ReconfigurationScenario.EvaluateConstraint.spread);
        thread.execute(spread);
        VerticalElasticity among = new VerticalElasticity(ReconfigurationScenario.EvaluateConstraint.among);
        thread.execute(among);
        VerticalElasticity gather = new VerticalElasticity(ReconfigurationScenario.EvaluateConstraint.gather);
        thread.execute(gather);

        thread.shutdown();

    }

}
