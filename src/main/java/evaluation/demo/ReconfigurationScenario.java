package evaluation.demo;

import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.constraint.CMaxOnlines;
import evaluation.generator.ConverterTools;

import java.util.Collection;

/**
 * User: Tu Huynh Dang
 * Date: 6/16/13
 * Time: 2:52 PM
 */
public abstract class ReconfigurationScenario implements Runnable {

    int modelId;
    Model model;
    Collection<SatConstraint> validateConstraint;
    ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();

    abstract boolean discrete(int p);

    abstract boolean tryContinuous(int p);

    public void readData(int id) {
        String path = System.getProperty("user.home") + System.getProperty("file.separator") + "model"
                + System.getProperty("file.separator");
        model = ConverterTools.getModelFromFile(path + "model" + id + ".json");
        validateConstraint = ConverterTools.getConstraints(model, path + "constraints" + id + ".json");
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
    }
}
