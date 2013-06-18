package evaluation.demo;

import btrplace.model.*;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.constraint.CMaxOnlines;
import btrplace.solver.choco.constraint.CMaxSpareResources;
import evaluation.generator.ConverterTools;

import java.util.*;

/**
 * User: Tu Huynh Dang
 * Date: 6/11/13
 * Time: 10:14 PM
 */
public class ModelMaker implements Runnable {


    final static int NUM_RACK = 16;
    final static int NODES_PER_RACK = 16;
    Model model;
    boolean restriction;
    ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    ReconfigurationPlan plan = null;
    Collection<Collection<Node>> racks;
    Collection<Collection<Node>> groups;
    Collection<SatConstraint> validateConstraint;
    ArrayList<Application> appList;

    private static int modelId = 1;

    public ModelMaker() {
        model = new DefaultModel();
        restriction = false;
        racks = new ArrayList<Collection<Node>>();
        groups = new ArrayList<Collection<Node>>();
        validateConstraint = new ArrayList<SatConstraint>();
        appList = new ArrayList<Application>();
        cra.setTimeLimit(300);
    }

    public static void main(String[] args) {
        ModelMaker modelMaker = new ModelMaker();
        modelMaker.run();
    }

    private void initMap() {
        ShareableResource cpu = new ShareableResource("cpu", 32, 1);
        model.attach(cpu);
        ShareableResource ram = new ShareableResource("ram", 128, 1);
        model.attach(ram);
        Collection<Node> group1 = new ArrayList<Node>();
        Collection<Node> group2 = new ArrayList<Node>();
        for (int j = 0; j < NUM_RACK; j++) {
            ArrayList<Node> ns = new ArrayList<Node>();
            for (int i = 0; i < NODES_PER_RACK; i++) {
                Node node = model.newNode();
                ns.add(node);
                model.getMapping().addOnlineNode(node);
            }
            racks.add(ns);
            if (j < 8) group1.addAll(ns);
            else group2.addAll(ns);
        }
        groups.add(group1);
        groups.add(group2);
    }

    public void run() {
        initMap();
        runMix();
        storeModel(true);
    }


    private boolean runMix() {
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();

        try {
            Collection<Application> tmp = new ArrayList<Application>();
            for (int i = 0; i < 450; i++) {
                Application app = new Application(model);
                runApplication(app, cstrs);
                tmp.add(app);
                validateConstraint.addAll(app.spread(restriction));
                Among among = new Among(app.getDatabaseVM(), racks, restriction);
                validateConstraint.add(among);
            }
            cstrs.addAll(addSplitAmong());
            SingleResourceCapacity SReC = new SingleResourceCapacity(model.getNodes(), "cpu", 30, restriction);
            SingleResourceCapacity SReC2 = new SingleResourceCapacity(model.getNodes(), "ram", 120, restriction);
            MaxOnline maxOnline = new MaxOnline(model.getNodes(), 250, restriction);
            validateConstraint.add(maxOnline);
            validateConstraint.add(SReC);
            validateConstraint.add(SReC2);
            cstrs.addAll(validateConstraint);
            plan = cra.solve(model, cstrs);
            if (plan != null) {
                model = plan.getResult();
                appList.addAll(tmp);
                System.out.println("Init Success");
                System.out.println(model);
            }
        } catch (SolverException e) {
            e.printStackTrace();
            System.err.println("Run " + e.getMessage());
            return false;
        }
        return true;
    }

    private int currentLoad() {
        Mapping mapping = model.getMapping();
        Set<Node> onlineNodes = mapping.getOnlineNodes();
        Set<VM> runningVMs = mapping.getRunningVMs();
        ShareableResource sr = (ShareableResource) model.getView("ShareableResource.cpu");
        double capacity = sr.sumCapacities(onlineNodes, true); // * overbook.getRatio() ;
        double used = sr.sumConsumptions(runningVMs, true);
        return (int) (used / capacity * 100);
    }

    private void runApplication(Application a, Collection<SatConstraint> constraints) {
        Running run = new Running(a.getAllVM());
        constraints.add(run);
    }

    private Collection<SatConstraint> addSplitAmong() {
        Collection<SatConstraint> constraints = new ArrayList<SatConstraint>();
        Collection<Collection<VM>> vms = new ArrayList<Collection<VM>>();
        Application app1 = new Application(model);
        Application app2 = new Application(model);
        vms.add(app1.getAllVM());
        vms.add(app2.getAllVM());
        SplitAmong splitAmong = new SplitAmong(vms, groups);
        constraints.add(new Running(app1.getAllVM()));
        constraints.add(new Running(app2.getAllVM()));
        constraints.add(splitAmong);
        return constraints;
    }

    private void runOneALonelyApplication(Collection<SatConstraint> constraints) {
        Application app = new Application(model);
        runApplication(app, constraints);
        validateConstraint.add(new Lonely(new HashSet<VM>(app.getAllVM()), restriction));
    }

    private Collection<SatConstraint> loadSpike(Application app) {
        Collection<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(new Preserve(app.getTier2(), "cpu", 8));
        constraints.add(new Preserve(app.getTier2(), "ram", 7));
        return constraints;
    }

    private void storeModel(boolean store) {
        if (store) {
            String path = System.getProperty("user.home") + System.getProperty("file.separator") + "model"
                    + System.getProperty("file.separator");
            ConverterTools.modelToFile(model, path + "model" + modelId + ".json");
            ConverterTools.constraintsToFile(validateConstraint, path + "constraints" + modelId++ + ".json");
        }
    }
}
