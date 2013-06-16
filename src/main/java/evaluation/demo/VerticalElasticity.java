package evaluation.demo;

import btrplace.model.DefaultModel;
import btrplace.model.Node;
import btrplace.model.constraint.Among;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: Tu Huynh Dang
 * Date: 6/11/13
 * Time: 10:14 PM
 */
public class VerticalElasticity extends ReconfigurationScenario implements Runnable {

    public VerticalElasticity(EvaluateConstraint constraint) {
        model = new DefaultModel();
        restriction = false;
        eval_constraint = constraint;
        nss = new ArrayList<Collection<Node>>();
        cra.setTimeLimit(30);
    }

    public void run() {
        initMap();
        Collection<Application> appList = new ArrayList<Application>();

        switch (eval_constraint) {
            case spread:
                appList.addAll(runWithSpread());
                break;
            case among:
                appList.addAll(runWithAmong());
                break;
            case gather:
                appList.addAll(runWithGather());
                break;
        }


        int count = 0;
        boolean satisfied = true;
        int p = 20;
        try {
            do {
                count++;
                ReconfigurationPlan pl = reconfigure(appList, p);
                if (pl == null) {
                    break;
                }
                p += 5;
                for (Application app : appList) {
                    for (SatConstraint s : app.getCheckConstraints()) {
                        boolean continuous = s.isContinuous();
                        if (!continuous) s.setContinuous(true);
                        if (!s.isSatisfied(pl)) {
                            satisfied = false;
//                            System.err.println("Does not satisfy " + s);
                            break;
                        }
                        s.setContinuous(continuous);
                    }
                    if (!satisfied) break;
                }
            } while (satisfied && p < 100);
        } finally {
            System.out.printf("%s\t%d\t%d\t%d\t%d\n",eval_constraint, appList.size(), currentLoad(), p, count);
        }
    }



    public ReconfigurationPlan reconfigure(Collection<Application> apps, int p) {
        cra.setTimeLimit(30);
        Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        int i = 0;
        for (Application a : apps) {
            cstrs.addAll(a.getCheckConstraints());
            if (i++ % 2 == 0) continue;
            cstrs.addAll(a.loadSpike(p));
        }
        ReconfigurationPlan plan = null;
        try {
            plan = cra.solve(model, cstrs);
        } catch (SolverException e) {
            System.err.println("Reconfig: " + e.getMessage());
        }
        return plan;
    }
}
