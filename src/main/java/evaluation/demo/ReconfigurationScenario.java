package evaluation.demo;

import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.constraint.CMaxOnlines;
import evaluation.generator.ConverterTools;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: Tu Huynh Dang
 * Date: 6/16/13
 * Time: 2:52 PM
 */
public abstract class ReconfigurationScenario implements Runnable {

    int modelId;
    Model model;
    ArrayList<SatConstraint> validateConstraint;
    Collection<Application> applications;
    ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    StringBuilder sb;
    ShareableResource ecu;
    ShareableResource ram;

    abstract boolean reconfigure(int p, boolean c);

    public void readData(int id) {
        String path = System.getProperty("user.home") + System.getProperty("file.separator") + "model"
                + System.getProperty("file.separator");
        model = ConverterTools.getModelFromFile(path + "model" + id + ".json");
        validateConstraint = ConverterTools.getConstraints(model, path + "constraints" + id + ".json");
        applications = ConverterTools.getApplicationsFromFile(model, path + "applications" + id + ".json");
        ecu = (ShareableResource) model.getView(ShareableResource.VIEW_ID_BASE + "ecu");
        ram = (ShareableResource) model.getView(ShareableResource.VIEW_ID_BASE + "ram");
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
    }
}
