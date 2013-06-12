package evaluation.demo;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.constraint.Among;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.SingleRunningCapacity;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import evaluation.generator.EvaluationTools;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: Tu Huynh Dang
 * Date: 6/11/13
 * Time: 10:14 PM
 */
public class LoadPeak {
    static Model model = new DefaultModel();

    public static void main(String[] args) {

        ShareableResource cpu = new ShareableResource("cpu", 16, 1);
        Collection<Application> appList = new ArrayList<Application>();
        model.attach(cpu);

        ArrayList<Collection<Node>> nss = new ArrayList<Collection<Node>>();
        for (int j = 0; j < 8; j++) {
            ArrayList<Node> ns = new ArrayList<Node>();
            for (int i = 0; i < 20; i++) {
                Node node = model.newNode();
                ns.add(node);
                model.getMapping().addOnlineNode(node);
            }
            nss.add(ns);
        }
        Model clone = model.clone();

        try {
            boolean cont;
            int count = 0;
            int load;
            do {
                Application app = new Application(model);
                appList.add(app);
                // Set Spread, Among continuous restriction by the third parameter
                cont = runApp(app, nss, false);

                Assert.assertNotEquals(model, clone);
//                System.out.println(String.format("Runned %d applications", ++count));
                System.out.print(".");
                load = EvaluationTools.currentLoad(model);
            }
            while (cont && load < 80);

        } catch (SolverException e) {
            System.err.println("Cannot host more applications");
        }
        System.out.println("Current Load: " + EvaluationTools.currentLoad(model));
        System.out.println(model.getMapping());
        boolean satisfied = true;
        do {
            ReconfigurationPlan pl = reconfigureWhenLoadSpike(appList);

            for (Application app : appList) {
                for (SatConstraint s : app.getCheckConstraints()) {
                    boolean continuous = s.isContinuous();
                    if (!continuous) s.setContinuous(true);
                    if (!s.isSatisfied(pl)) {
                        satisfied = false;
                        System.out.println("Does not satisfy " + s);
                        System.exit(-1);
                    }
                    s.setContinuous(continuous);
                }
            }
        } while (satisfied);
        System.out.println("Continuous satisfaction");

    }

    public static boolean runApp(Application app, Collection<Collection<Node>> nodeSets, boolean cont) throws SolverException {
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setTimeLimit(10);
        Collection<SatConstraint> cstrs = app.initConstraints(cont);
        Among among = new Among(app.getDatabaseVM(), nodeSets, cont);
        for (Collection<Node> nodes : nodeSets) {
            SingleRunningCapacity src = new SingleRunningCapacity(nodes, 6);
            cstrs.add(src);
        }
        cstrs.add(among);
        ReconfigurationPlan plan = cra.solve(model, cstrs);

        if (plan != null) {
            model = plan.getResult();
            return true;
        }
        return false;
    }

    public static ReconfigurationPlan reconfigureWhenLoadSpike(Collection<Application> apps) {
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        for (Application a : apps) {
            cstrs.addAll(a.loadSpike(30));
            cstrs.addAll(a.getCheckConstraints());
        }
        try {
            ReconfigurationPlan plan = cra.solve(model, cstrs);
            System.out.println(cra.getSolvingStatistics());
            return plan;
        } catch (SolverException e) {
            System.err.println("No problem");
            return null;
        }
    }

}
