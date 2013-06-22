package evaluation.scenarios;

import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.SolvingStatistics;
import evaluation.demo.Application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * User: TU HUYNH DANG
 * Date: 6/18/13
 * Time: 11:34 AM
 */
public class VerticalElasticity extends ReconfigurationScenario {


    public VerticalElasticity(int id) {
        modelId = id;
        validateConstraint = new ArrayList<>();
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
        int p = 20;
        reconfigure(p, false);
        reconfigure(p, true);
        System.out.print(sb.toString());
    }

    @Override
    public boolean reconfigure(int p, boolean c) {
        int[] vioTime = new int[5];
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
        constraints.add(new Preserve(tier2, "ecu", 2));
        constraints.add(new Preserve(tier2, "ram", 4));
        constraints.add(new Preserve(tier2, "ecu", 14));
        constraints.add(new Preserve(tier2, "ram", 7));
        constraints.add(new Preserve(tier2, "ecu", 4));
        constraints.add(new Preserve(tier2, "ram", 17));
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
                for (SatConstraint s : validateConstraint) {
                    boolean continuous = s.isContinuous();
                    if (!continuous) s.setContinuous(true);
                    if (!s.isSatisfied(plan)) {
                        satisfied = false;
                        if (s instanceof Spread) {
                            vioTime[0]++;
                        } else if (s instanceof Among) {
                            vioTime[1]++;
                        } else if (s instanceof SplitAmong) {
                            vioTime[2]++;
                        } else if (s instanceof SingleResourceCapacity) {
                            vioTime[3]++;
                        } else if (s instanceof MaxOnline) {
                            vioTime[4]++;
                        }
                    }
                    s.setContinuous(continuous);
                }
            }
        } catch (SolverException e) {
            sb.append(String.format("Model %d.\t%b\t%s\n", modelId, c, e.getMessage()));
            return false;
        }
        sb.append(String.format("%-2d\t%b\t%-3d\t%-2d\t%d\t%d\t%d\t%d\t", modelId, c, p,
                vioTime[0], vioTime[1], vioTime[2], vioTime[3], vioTime[4]));
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
