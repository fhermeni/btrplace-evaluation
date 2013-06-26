package evaluation.scenarios;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.constraint.CMaxOnlines;
import evaluation.demo.Application;
import evaluation.generator.ConverterTools;

import java.util.*;

/**
 * User: Tu Huynh Dang
 * Date: 6/16/13
 * Time: 2:52 PM
 */
public abstract class ReconfigurationScenario implements Runnable {

    static int TIME_OUT = 300;
    int modelId;
    Model model;
    Set<SatConstraint> validateConstraint;
    Map<SatConstraint, Integer> checkMap;
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
//        validateConstraint = ConverterTools.getConstraints(model, path + "constraints" + id + ".json");
        applications = ConverterTools.getApplicationsFromFile(model, path + "applications" + id + ".json");
        ecu = (ShareableResource) model.getView(ShareableResource.VIEW_ID_BASE + "ecu");
        ram = (ShareableResource) model.getView(ShareableResource.VIEW_ID_BASE + "ram");
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        checkMap = new HashMap<>(1200);
        for (Application a : applications) {
            for (SatConstraint s : a.getConstraints()) {
                checkMap.put(s, a.getId());
            }
        }
        validateConstraint = checkMap.keySet();
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

    public void checkSatisfaction(ReconfigurationPlan plan, ArrayList<ArrayList<Integer>> constr,
                                  int[] DCconstraint, HashSet<Integer> apps) {
        for (SatConstraint s : checkMap.keySet()) {
            Integer appId = checkMap.get(s);
            boolean continuous = s.isContinuous();
            if (!continuous) s.setContinuous(true);
            if (!s.isSatisfied(plan)) {
                if (s instanceof Spread) {
                    constr.get(0).add(appId);
                    apps.add(appId);
                } else if (s instanceof Among) {
                    constr.get(1).add(appId);
                    apps.add(appId);
                } else if (s instanceof SplitAmong) {
                    constr.get(2).add(appId);
                    apps.add(appId);
                } else if (s instanceof SingleResourceCapacity) {
                    DCconstraint[0]++;
                } else if (s instanceof MaxOnline) {
                    DCconstraint[1]++;
                }
            }
            s.setContinuous(continuous);
        }
    }

    public static void setTimeOut(int timeout) {
        TIME_OUT = timeout;
    }
}
