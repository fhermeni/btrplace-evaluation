package evaluation.demo;

import btrplace.model.*;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.constraint.CMaxOnlines;
import evaluation.generator.ConverterTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Tu Huynh Dang
 * Date: 6/11/13
 * Time: 10:14 PM
 */
public class ModelMaker implements Runnable {


    final static int NUM_RACK = 16;
    final static int NUM_APP = 350;
    final static int NODES_PER_RACK = 16;
    Model model;
    boolean restriction;
    ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    ReconfigurationPlan plan = null;
    Collection<Collection<Node>> racks;
    Collection<Collection<Node>> groups;
    Collection<SatConstraint> validateConstraint;
    ArrayList<Application> appList;
    ShareableResource ecu;
    ShareableResource ram;
    private int modelId;

    public ModelMaker(int id) {
        model = new DefaultModel();
        modelId = id;
        restriction = false;
        racks = new ArrayList<>();
        groups = new ArrayList<>();
        validateConstraint = new ArrayList<>();
        appList = new ArrayList<>();
        cra.setTimeLimit(300);
        ecu = new ShareableResource("ecu", 64, 1);
        ram = new ShareableResource("ram", 128, 1);
        model.attach(ecu);
        model.attach(ram);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 50; i++) {
            ModelMaker modelMaker = new ModelMaker(i);
            modelMaker.run();
        }
    }

    public void run() {
        initMap();
        runMix();
        float[] load = currentLoad();
        System.out.printf("%f, %f\n", load[0], load[1]);
        storeModel(true);
    }

    private void initMap() {
        Collection<Node> group1 = new ArrayList<>();
        Collection<Node> group2 = new ArrayList<>();
        for (int j = 0; j < NUM_RACK; j++) {
            ArrayList<Node> ns = new ArrayList<>();
            for (int i = 0; i < NODES_PER_RACK; i++) {
                Node node = model.newNode();
                ns.add(node);
                if (i == NODES_PER_RACK - 1)
                    model.getMapping().addOfflineNode(node);
                else model.getMapping().addOnlineNode(node);
            }
            racks.add(ns);
            if (j < 8) group1.addAll(ns);
            else group2.addAll(ns);
        }
//        System.out.println("size: " + model.getNodes().size());

        groups.add(group1);
        groups.add(group2);
    }

    private boolean runMix() {
        cra.getConstraintMapper().register(new CMaxOnlines.Builder());
        //cra.getConstraintMapper().register(new CMaxSpareResources.Builder());
        Collection<SatConstraint> constraints = new ArrayList<>();

        try {
            for (int i = 0; i < NUM_APP; i++) {
                Application app = new Application(model, i);
                runApplication(app, constraints);
                if (i % 4 == 0) makeHA(app, constraints);
            }
            SingleResourceCapacity SReC = new SingleResourceCapacity(model.getMapping().getAllNodes(), "ecu", 60, restriction);
            SingleResourceCapacity SReC2 = new SingleResourceCapacity(model.getMapping().getAllNodes(), "ram", 120, restriction);
            MaxOnline maxOnline = new MaxOnline(model.getMapping().getAllNodes(), 240, restriction);
            validateConstraint.add(maxOnline);
            validateConstraint.add(SReC);
            validateConstraint.add(SReC2);
            constraints.addAll(validateConstraint);
            plan = cra.solve(model, constraints);
            if (plan != null) {
                model = plan.getResult();
            }
        } catch (SolverException e) {
            e.printStackTrace();
            System.err.println("Run " + e.getMessage());
            return false;
        }
        return true;
    }

    private void runApplication(Application app, Collection<SatConstraint> constraints) {
        appList.add(app);
        for (VM vm : app.getTier2()) {
            ecu.setConsumption(vm, 4);
            ram.setConsumption(vm, 2);
        }

        for (VM vm : app.getTier3()) {
            ram.setConsumption(vm, 4);
        }
        Running run = new Running(app.getAllVM());
        constraints.add(run);
        Collection<SatConstraint> spread = app.spread(restriction);
        validateConstraint.addAll(spread);
        Among among = new Among(app.getTier3(), racks, restriction);
        app.addConstraint(among);
        validateConstraint.add(among);
//        System.out.println("Among appID:" + app.getId());
    }

    private void makeHA(Application app, Collection<SatConstraint> constraints) {
        Collection<Collection<VM>> vms = new ArrayList<>();
        Application clone = new Application(app);
        runApplication(clone, constraints);

        vms.add(app.getAllVM());
        vms.add(clone.getAllVM());

        SplitAmong splitAmong = new SplitAmong(vms, groups);
        app.addConstraint(splitAmong);
        validateConstraint.add(splitAmong);
//        System.out.println("HA:" + app.getId() + "\t" + clone.getId());
    }

    private float[] currentLoad() {
        float[] loads = new float[2];
        Mapping mapping = model.getMapping();
        Set<Node> onlineNodes = mapping.getOnlineNodes();
        Set<VM> runningVMs = mapping.getRunningVMs();
        ShareableResource sr = (ShareableResource) model.getView("ShareableResource.ecu");
        ShareableResource sr2 = (ShareableResource) model.getView("ShareableResource.ram");
        float capacity = sr.sumCapacities(onlineNodes, true);
        float used = sr.sumConsumptions(runningVMs, true);
        loads[0] = used / capacity * 100;
        capacity = sr2.sumCapacities(onlineNodes, true);
        used = sr2.sumConsumptions(runningVMs, true);
        loads[1] = used / capacity * 100;
        return loads;
    }

    private void runApplicationLonely(Application app, Collection<SatConstraint> constraints) {
        for (VM vm : app.getTier2()) {
            ecu.setConsumption(vm, 4);
            ram.setConsumption(vm, 2);
        }

        for (VM vm : app.getTier3()) {
            ram.setConsumption(vm, 4);
        }
        Running run = new Running(app.getAllVM());
        Lonely lonely = new Lonely(new HashSet<>(app.getAllVM()));
        validateConstraint.add(lonely);
        constraints.add(run);
    }


/*    private void addSplitAmong(Collection<SatConstraint> constraints) {
        Collection<Collection<VM>> vms = new ArrayList<>();
        Application app1 = new Application(model);
        Application app2 = new Application(model);
        runApplication(app1, constraints);
        runApplication(app2, constraints);
        vms.add(app1.getAllVM());
        vms.add(app2.getAllVM());
        SplitAmong splitAmong = new SplitAmong(vms, groups);
        constraints.add(splitAmong);
        validateConstraint.add(splitAmong);
    }*/

    private void storeModel(boolean store) {
        if (store) {
            /*String path = System.getProperty("user.home") + System.getProperty("file.separator") + "model"
                    + System.getProperty("file.separator");*/
            ConverterTools.modelToFile(model, "./" + modelId + ".json");
//            ConverterTools.constraintsToFile(validateConstraint, path + "constraints" + modelId + ".json");
            ConverterTools.applicationsToFile(model, appList, "./" + "applications" + modelId + ".json");


        }
    }
}
