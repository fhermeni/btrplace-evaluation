package evaluation.demo;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Tu Huynh Dang
 * Date: 6/16/13
 * Time: 2:52 PM
 */
public abstract class ReconfigurationScenario {
    final static int NUM_RACK = 16;
    final static int NODES_PER_RACK = 16;
    Model model;
    boolean restriction;
    EvaluateConstraint CType;
    ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    ReconfigurationPlan plan = null;
    Collection<Collection<Node>> groups;
    Collection<SatConstraint> validateConstraint;
    ArrayList<Application> appList;
//    Overbook overbook;

    public abstract boolean reconfigure(int p) throws SolverException;

    public void setRestriction(boolean restriction) {
        this.restriction = restriction;
    }

    public void initMap() {
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
            if (j < 4)
                group1.addAll(ns);
            else
                group2.addAll(ns);
        }
        groups.add(group1);
        groups.add(group2);
    }

    public void runWithSpread() {
        try {
            do {
                Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();
                for (int i = 0; i < 500; i++) {
                    Application app = new Application(model);
                    runApplication(app, cstrs);
                    validateConstraint.addAll(app.spread(restriction));
                    appList.add(app);
                }
                cstrs.addAll(validateConstraint);
                plan = cra.solve(model, cstrs);
                if (plan != null) {
                    model = plan.getResult();
                }
            } while ((plan != null) && currentLoad() < 70);
        } catch (SolverException e) {
            System.err.println("Run " + e.getMessage());
        }
    }

    public void runWithGather() {
        try {
            do {
                Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();
                for (int i = 0; i < 500; i++) {
                    Application app = new Application(model);
                    runApplication(app, cstrs);
                    validateConstraint.addAll(app.gather(restriction));
                    appList.add(app);
                }
                cstrs.addAll(validateConstraint);
                plan = cra.solve(model, cstrs);
                if (plan != null) {
                    model = plan.getResult();
                }
            } while ((plan != null) && currentLoad() < 70);
        } catch (SolverException e) {
            System.err.println("Run " + e.getMessage());
        }
    }

    public void runWithAmong() {
        try {
            do {
                Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();
                for (int i = 0; i < 500; i++) {
                    Application app = new Application(model);
                    runApplication(app, cstrs);
                    Among among = new Among(app.getDatabaseVM(), groups, restriction);
                    validateConstraint.add(among);
                    appList.add(app);
                }
                cstrs.addAll(validateConstraint);
                plan = cra.solve(model, cstrs);
                if (plan != null) model = plan.getResult();
            } while ((plan != null) && currentLoad() < 70);

        } catch (SolverException e) {
            System.err.println("Run " + e.getMessage());
        }
    }

    public void runWithLonely() {
        Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        try {
            for (int i = 0; i < 450; i++) {
                Application app = new Application(model);
                runApplication(app, cstrs);
                appList.add(app);
            }
            plan = cra.solve(model, cstrs);
            if (plan != null) model = plan.getResult();

            do {
                validateConstraint.add(new Application(model).lonely(restriction));
                cstrs.addAll(validateConstraint);
                plan = cra.solve(model, cstrs);
                if (plan != null) model = plan.getResult();

            } while ((plan != null) && currentLoad() < 70);
        } catch (SolverException e) {
            System.err.println("Run " + e.getMessage());
        }
    }

    public void runWithSReC() {
        try {
            do {
                Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();
                for (int i = 0; i < 550; i++) {
                    Application app = new Application(model);
                    runApplication(app, cstrs);
                    appList.add(app);
                }
                SingleResourceCapacity SReC = new SingleResourceCapacity(model.getNodes(), "cpu", 30);
                validateConstraint.add(SReC);
                cstrs.addAll(validateConstraint);
                plan = cra.solve(model, cstrs);
                if (plan != null) model = plan.getResult();

            } while ((plan != null) && currentLoad() < 70);

        } catch (SolverException e) {
            System.err.println("Run " + e.getMessage());
        }
    }

    public boolean runMix() {
        Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();
//        cstrs.add(overbook);
        try {

            Collection<Application> tmp = new ArrayList<Application>();
            for (int i = 0; i < 450; i++) {
                Application app = new Application(model);
                runApplication(app, cstrs);
                tmp.add(app);
                validateConstraint.addAll(app.spread(restriction));
                Among among = new Among(app.getDatabaseVM(), groups, restriction);
//                validateConstraint.add(among);
            }
//            runOneALonelyApplication(cstrs);
            SingleResourceCapacity SReC = new SingleResourceCapacity(model.getNodes(), "cpu", 30);
            SingleResourceCapacity SReC2 = new SingleResourceCapacity(model.getNodes(), "ram", 120);
            validateConstraint.add(SReC);
            validateConstraint.add(SReC2);
            cstrs.addAll(validateConstraint);
            plan = cra.solve(model, cstrs);
            if (plan != null) {
                model = plan.getResult();
                appList.addAll(tmp);
            }
        } catch (SolverException e) {
            e.printStackTrace();
            System.err.println("Run " + e.getMessage());
            return false;
        }
        return true;
    }

    public int currentLoad() {
        Mapping mapping = model.getMapping();
        Set<Node> onlineNodes = mapping.getOnlineNodes();
        Set<VM> runningVMs = mapping.getRunningVMs();
        ShareableResource sr = (ShareableResource) model.getView("ShareableResource.cpu");
        double capacity = sr.sumCapacities(onlineNodes, true); // * overbook.getRatio() ;
        double used = sr.sumConsumptions(runningVMs, true);
        return (int) (used / capacity * 100);
    }

    public void runApplication(Application a, Collection<SatConstraint> constraints) {
        Running run = new Running(a.getAllVM());
        constraints.add(run);
    }

    public void runOneALonelyApplication(Collection<SatConstraint> constraints) {
        Application app = new Application(model);
        runApplication(app, constraints);
        validateConstraint.add(new Lonely(new HashSet<VM>(app.getAllVM()), restriction));
    }


    enum EvaluateConstraint {
        spread, among, gather, lonely, SReC, mixed

    }


}
