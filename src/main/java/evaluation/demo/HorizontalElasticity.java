package evaluation.demo;

import btrplace.model.constraint.SatConstraint;

import java.util.ArrayList;

/**
 * User: TU HUYNH DANG
 * Date: 6/18/13
 * Time: 5:06 PM
 */
public class HorizontalElasticity extends ReconfigurationScenario {

    StringBuilder sb;

    public HorizontalElasticity(int id) {
        modelId = id;
        validateConstraint = new ArrayList<SatConstraint>();
        sb = new StringBuilder();
        cra.setTimeLimit(60);
    }


    @Override
    boolean discrete(int p) {
        return true;
    }

    @Override
    boolean tryContinuous(int p) {
        return true;
    }

    @Override
    public void run() {
        readData(modelId);
        System.out.println(model.getMapping());
    }
}
