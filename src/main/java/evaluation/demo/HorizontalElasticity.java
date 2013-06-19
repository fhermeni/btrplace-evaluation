package evaluation.demo;

import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * User: TU HUYNH DANG
 * Date: 6/18/13
 * Time: 5:06 PM
 */
public class HorizontalElasticity extends ReconfigurationScenario {

    Collection<VM> cloneVMs;

    public HorizontalElasticity(int id) {
        modelId = id;
        validateConstraint = new ArrayList<SatConstraint>();
        sb = new StringBuilder();
        cloneVMs = new ArrayList<VM>();
        cra.setTimeLimit(300);
    }

    public static void main(String[] args) {
        HorizontalElasticity he = new HorizontalElasticity(1);
        he.sb.append("Model\tP\tS\tA\tSA\tSReC\tMO\tcontinuous\n");
        he.run();
    }


    @Override
    boolean reconfigure(int p, boolean c) {
        int[] vioTime = new int[5];
        boolean satisfied = true;
        Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        ReconfigurationPlan plan;


        Running run = new Running(cloneVMs);
        cstrs.add(run);
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
                        } else if (s instanceof SingleResourceCapacity) {
                            vioTime[2]++;
                        } else if (s instanceof MaxOnline) {
                            vioTime[3]++;
                        }
                    }
                    s.setContinuous(continuous);
                }
            }
        } catch (SolverException e) {
            sb.append(String.format("Model %d. %b: %s\n", modelId, c, e.getMessage()));
            return false;
        }
        sb.append(String.format("%d\t%d\t%d\t%d\t%d\t%d\t%d\t%b\n%s\n", modelId, p,
                vioTime[0], vioTime[1], vioTime[2], vioTime[3], vioTime[4], c, cra.getSolvingStatistics()));
        return satisfied;
    }


    @Override
    public void run() {
        readData(modelId);
        int p = 5;
        int count = 20; // 5% number of applications
        Collections.shuffle(new ArrayList<Object>(validateConstraint));
        for (SatConstraint s : validateConstraint) {
            if (s instanceof Among) {
                int size = s.getInvolvedVMs().size() * 8;
                int id = model.getMapping().getAllVMs().size();
                Collection<VM> tmp = new ArrayList<VM>();
                for (int i = 0; i < size; i++) {
                    VM clone = model.newVM(id++);
                    model.getMapping().addReadyVM(clone);
                    tmp.add(clone);
                }
                s.getInvolvedVMs().addAll(tmp);
                cloneVMs.addAll(tmp);
                count--;
            }
            if (count <= 0) break;
        }

        reconfigure(p, false);
        reconfigure(p, true);
        System.out.println(this);
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
