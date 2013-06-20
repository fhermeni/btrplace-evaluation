package evaluation.scenarios;

import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: TU HUYNH DANG
 * Date: 6/19/13
 * Time: 10:05 AM
 */
public class BootStorm extends ReconfigurationScenario {

    public BootStorm(int id) {
        modelId = id;
        validateConstraint = new ArrayList<>();
        sb = new StringBuilder();
        cra.setTimeLimit(TIME_OUT);
    }

    public static void main(String[] args) {
        ReconfigurationScenario instance = new BootStorm(1);
        instance.sb.append("Model\tP\tS\tA\tSA\tSReC\tMO\tcontinuous\n");
        instance.run();
    }

    @Override
    public void run() {
        readData(modelId);
        int p = 100;
        reconfigure(p, false);
        reconfigure(p, true);
        System.out.println(this);
    }

    @Override
    public boolean reconfigure(int p, boolean c) {
        int[] vioTime = new int[5];
        boolean satisfied = true;
        Collection<SatConstraint> cstrs = new ArrayList<>();
        ReconfigurationPlan plan;
        Collection<VM> bootVMS = new ArrayList<>();
        for (int i = 0; i < p; i++) {
            VM vm = model.newVM();
            model.getMapping().addReadyVM(vm);
            bootVMS.add(vm);
        }
        cstrs.add(new Running(bootVMS));
        cstrs.add(new Preserve(bootVMS, "ecu", 2));
        cstrs.add(new Preserve(bootVMS, "ram", 4));
        if (c) {
            for (SatConstraint s : validateConstraint) {
                s.setContinuous(true);
            }
        }
        cstrs.addAll(validateConstraint);
        try {
            plan = cra.solve(model, cstrs);
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
