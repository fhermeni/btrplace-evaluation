package evaluation.demo;

import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;

import java.util.*;

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
        cra.setTimeLimit(300);
    }

    public static void main(String[] args) {
        ReconfigurationScenario instance = new VerticalElasticity(1);
        instance.sb.append("Model\tP\tS\tA\tSA\tSReC\tMO\tcontinuous\n");
        instance.run();
    }


    @Override
    public void run() {
        readData(modelId);
        int p = 5;
        reconfigure(p, false);
        reconfigure(p, true);
        System.out.println(sb.toString());
    }

    @Override
    public boolean reconfigure(int p, boolean c) {
        int[] vioTime = new int[5];
        boolean satisfied = true;
        Collection<SatConstraint> cstrs = new ArrayList<>();
        ReconfigurationPlan plan;
        int size = model.getMapping().getAllVMs().size() * p / 100;
        List<VM> vms = new ArrayList<>(model.getMapping().getAllVMs());
        Collections.shuffle(vms);
        Iterator<VM> iterator = vms.iterator();
        Collection<VM> vmSpike = new ArrayList<>();
        while (iterator.hasNext() && size > 0) {
            vmSpike.add(iterator.next());
            size--;
        }
        cstrs.add(new Preserve(vmSpike, "ecu", 14));
        cstrs.add(new Preserve(vmSpike, "ram", 7));
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
        sb.append(String.format("%d\t%d\t%d\t%d\t%d\t%d\t%d\t%b\n", modelId, p,
                vioTime[0], vioTime[1], vioTime[2], vioTime[3], vioTime[4], c));
        return satisfied;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

}
