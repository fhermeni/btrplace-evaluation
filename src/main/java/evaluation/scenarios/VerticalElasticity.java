package evaluation.scenarios;

import btrplace.model.VM;
import btrplace.model.constraint.Preserve;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.SolvingStatistics;
import evaluation.demo.Application;
import evaluation.generator.ConverterTools;

import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 6/18/13
 * Time: 11:34 AM
 */
public class VerticalElasticity extends ReconfigurationScenario {


    public VerticalElasticity(int id) {
        modelId = id;
        sb = new StringBuilder();
        cra.setTimeLimit(TIME_OUT);
        cra.doRepair(true);
    }

    public static void main(String[] args) {
        ReconfigurationScenario instance = new VerticalElasticity(1);
        instance.run();
    }


    @Override
    public void run() {
        readData(modelId);
        int p = 10;
        reconfigure(p, false);
        reconfigure(p, true);
        System.out.print(sb.toString());
    }

    @Override
    public boolean reconfigure(int p, boolean c) {
        int DCconstraint[] = new int[2];
        ArrayList<ArrayList<Integer>> violatedApp = new ArrayList<>();
        HashSet<Integer> affectedApps = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            violatedApp.add(new ArrayList<Integer>());
        }
        boolean satisfied = true;
        Collection<SatConstraint> constraints = new ArrayList<>();
        ReconfigurationPlan plan;
        Collections.shuffle(new ArrayList<>(applications));
        int size = applications.size() * p / 100;
        Iterator<Application> iterator = applications.iterator();
        Collection<VM> tier1 = new ArrayList<>();
        Collection<VM> tier2 = new ArrayList<>();
        Collection<VM> tier3 = new ArrayList<>();
        while (iterator.hasNext() && size > 0) {
            Application randomApp = iterator.next();
            tier1.addAll(randomApp.getTier1());
            tier2.addAll(randomApp.getTier2());
            tier3.addAll(randomApp.getTier3());
            size--;
        }
        constraints.add(new Preserve(tier1, "ecu", 2));
        constraints.add(new Preserve(tier1, "ram", 4));
        constraints.add(new Preserve(tier2, "ecu", 14));
        constraints.add(new Preserve(tier2, "ram", 7));
        constraints.add(new Preserve(tier3, "ecu", 4));
        constraints.add(new Preserve(tier3, "ram", 17));

        if (c) {
            for (SatConstraint s : validateConstraint) {
                s.setContinuous(true);
            }
        }
        constraints.addAll(validateConstraint);
        try {
            plan = cra.solve(model, constraints);
            if (plan == null) {
                sb.append(String.format("Model %d\t %b \t No solution\n", modelId, c));
                return false;
            } else {
                checkSatisfaction(plan, violatedApp, DCconstraint, affectedApps);
            }
        } catch (SolverException e) {
            sb.append(String.format("Model %d.\t%b\t%s\n", modelId, c, e.getMessage()));
            return false;
        }
        String path = System.getProperty("user.home") + System.getProperty("file.separator") + "plan"
                + System.getProperty("file.separator") + "ve" + System.getProperty("file.separator");

        ConverterTools.planToFile(plan, String.format("%s%d%b", path, modelId, c));

        sb.append(String.format("%-2d\t%b\t%-3d\t%-2d\t%d\t%d\t%d\t%d\t%d\t", modelId, c, p,
                violatedApp.get(0).size(), violatedApp.get(1).size(), violatedApp.get(2).size(),
                DCconstraint[0], DCconstraint[1], affectedApps.size()));
        float[] load = currentLoad(model);
        sb.append(String.format("%f\t%f\t", load[0], load[1]));
        load = currentLoad(plan.getResult());
        sb.append(String.format("%f\t%f\t", load[0], load[1]));
        SolvingStatistics statistics = cra.getSolvingStatistics();
        sb.append(String.format("%d\t%d\t%d\n", statistics.getSolvingDuration(), plan.getDuration(), plan.getSize()));
        return satisfied;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
