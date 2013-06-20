package evaluation.scenarios;

import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
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
    }

    public static void main(String[] args) {
        ReconfigurationScenario instance = new VerticalElasticity(1);
        instance.sb.append("Model\tP\tS\tA\tSA\tSReC\tMO\tcontinuous\n");
        instance.run();
    }


    @Override
    public void run() {
        readData(modelId);
        int p = 20;
        reconfigure(p, false);
        reconfigure(p, true);
        System.out.println(sb.toString());
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
                sb.append(String.format("Model %d. %d %b No solution\n", modelId, p, c));
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
            sb.append(String.format("Model %d. %b: %s\n", modelId, c, e.getMessage()));
            return false;
        }
        sb.append(String.format("%-2d\t%-3d\t%-2d\t%d\t%d\t%d\t%d\t%b\n", modelId, p,
                vioTime[0], vioTime[1], vioTime[2], vioTime[3], vioTime[4], c));
        float[] load = currentLoad(model);
        sb.append(String.format("Before RP. CPU:\t%f\tRAM:%f\n", load[0], load[1]));
        load = currentLoad(plan.getResult());
        sb.append(String.format("After RP. CPU:\t%f\tRAM:%f\n", load[0], load[1]));
        sb.append(cra.getSolvingStatistics());
        return satisfied;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

}
