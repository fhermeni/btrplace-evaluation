package evaluation.scenarios;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.constraint.CMaxOnlines;
import evaluation.demo.Application;
import evaluation.generator.ConverterTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

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
    static int TIME_OUT = 300;

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

    public static float[] currentLoad(Model measure_model) {
        float[] loads = new float[2];
        Mapping mapping = measure_model.getMapping();
        Set<Node> onlineNodes = mapping.getOnlineNodes();
        Set<VM> runningVMs = mapping.getRunningVMs();
        ShareableResource sr = (ShareableResource) measure_model.getView("ShareableResource.ecu");
        ShareableResource sr2 = (ShareableResource) measure_model.getView("ShareableResource.ram");
        float capacity = sr.sumCapacities(onlineNodes, true);
        float used = sr.sumConsumptions(runningVMs, true);
        loads[0] = used / capacity * 100;
        capacity = sr2.sumCapacities(onlineNodes, true);
        used = sr2.sumConsumptions(runningVMs, true);
        loads[1] = used / capacity * 100;
        return loads;
    }

    public static void setTimeOut(int timeout) {
        TIME_OUT = timeout;
    }
}
