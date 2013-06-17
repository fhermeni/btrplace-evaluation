package evaluation.demo;

import btrplace.model.DefaultModel;
import btrplace.model.Node;
import btrplace.model.constraint.SatConstraint;
import btrplace.solver.SolverException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * User: Tu Huynh Dang
 * Date: 6/11/13
 * Time: 10:14 PM
 */
public class VerticalElasticity extends ReconfigurationScenario implements Runnable {

    private int PERCENT_APPS_INC = 5;

    public VerticalElasticity(EvaluateConstraint constraint) {
        model = new DefaultModel();
        restriction = true;
        CType = constraint;
        groups = new ArrayList<Collection<Node>>();
        validateConstraint = new ArrayList<SatConstraint>();
        appList = new ArrayList<Application>();
//        overbook = new Overbook(model.getNodes(), "cpu", 4);

    }

    public void run() {
        initMap();
        boolean evaluate = true;
        switch (CType) {
            case spread:
                runWithSpread();
                break;
            case among:
                runWithAmong();
                break;
            case gather:
                runWithGather();
                break;
            case lonely:
                runWithLonely();
                break;
            case mixed:
                evaluate = runMix();
        }
        if (evaluate) {
            int count = 0;
            int p = 30;
            try {
                do {
                    if (!reconfigure(p)) break;
                    count++;
                    p += 5;
                } while (p < 100);
            } finally {
                System.out.printf("%s\t%d\t%d\t%d\t%d\n", CType, appList.size(), currentLoad(), p, count);
            }
        }
    }


    public boolean reconfigure(int p) {
        boolean satisfied = true;
        Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        int n_apps = appList.size() * PERCENT_APPS_INC / 100;
        cstrs.addAll(validateConstraint);
        Collections.shuffle(appList);
        Iterator<Application> iterator = appList.iterator();
        while (iterator.hasNext() && (n_apps-- > 0)) {
            Application a = iterator.next();
            cstrs.addAll(a.loadSpike());
        }
        /*for (SatConstraint s : validateConstraint) {
            System.out.println(s);
        }*/
        try {
            plan = cra.solve(model, cstrs);
            if (plan == null) {
                System.out.println(cra.getSolvingStatistics());
                return false;
            } else {
                for (SatConstraint s : validateConstraint) {
                    boolean continuous = s.isContinuous();
                    if (!continuous) s.setContinuous(true);
                    if (!s.isSatisfied(plan)) {
                        satisfied = false;
                        System.err.println("Does not satisfy " + s);
                    }
                    s.setContinuous(continuous);
                }
            }
        } catch (SolverException e) {
            System.err.println("Reconfig: " + e.getMessage());
        }

        return satisfied;
    }
}
