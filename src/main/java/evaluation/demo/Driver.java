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

        int nP = 50;

        for (int i = 1; i <= nP; i++) {
//            ReconfigurationScenario rs = new HorizontalElasticity(i);
//            thread.execute(rs);
            ModelMaker modelMaker = new ModelMaker(i);
            thread.execute(modelMaker);
        }
        thread.shutdown();

    }

}
