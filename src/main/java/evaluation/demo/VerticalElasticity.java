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

    StringBuilder sb;

    public VerticalElasticity(int id) {
        modelId = id;
        validateConstraint = new ArrayList<SatConstraint>();
        sb = new StringBuilder();
        cra.setTimeLimit(60);
    }


    @Override
    public void run() {
        int p = 5;
        boolean c;

        readData(modelId);

        do {
            c = discrete(p);
            if (!c) break;
            p += 5;
        } while (c && p < 100);
        tryContinuous(p);
        System.out.println(sb.toString());
    }

    @Override
    public boolean discrete(int p) {
        int[] vioTime = new int[4];
        boolean satisfied = true;
        Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        ReconfigurationPlan plan;
        int size = model.getMapping().getAllVMs().size() * p / 100;
        List<VM> vms = new ArrayList<VM>(model.getMapping().getAllVMs());
        Collections.shuffle(vms);
        Iterator<VM> iterator = vms.iterator();
        Collection<VM> vmSpike = new ArrayList<VM>();
        while (iterator.hasNext() && size > 0) {
            vmSpike.add(iterator.next());
            size--;
        }
        cstrs.add(new Preserve(vmSpike, "cpu", 2));
        cstrs.add(new Preserve(vmSpike, "ram", 3));
        cstrs.addAll(validateConstraint);
        try {
            plan = cra.solve(model, cstrs);
            if (plan == null) {
                sb.append(String.format("Model %2d. %d Discrete No solution\n", modelId, p));
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
//                        System.err.println("Does not satisfy continuous restriction");
                    }
                    s.setContinuous(continuous);
                }
            }
        } catch (SolverException e) {
            sb.append(String.format("Model %2d. Discrete: %s\n", modelId, e.getMessage()));
            return false;
        }
        sb.append(String.format("%2d\t%2d\t%2d\t%3d\t%d\t%d\tD\n", modelId, p,
                vioTime[0], vioTime[1], vioTime[2], vioTime[3]));
        return satisfied;
    }

    public boolean tryContinuous(int p) {
        int[] vioTime = new int[4];
        boolean satisfied = true;
        Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        ReconfigurationPlan plan;
        int size = model.getMapping().getAllVMs().size() * p / 100;
        List<VM> vms = new ArrayList<VM>(model.getMapping().getAllVMs());
        Collections.shuffle(vms);
        Iterator<VM> iterator = vms.iterator();
        Collection<VM> vmSpike = new ArrayList<VM>();
        while (iterator.hasNext() && size > 0) {
            vmSpike.add(iterator.next());
            size--;
        }
        cstrs.add(new Preserve(vmSpike, "cpu", 7));
        cstrs.add(new Preserve(vmSpike, "ram", 8));
        for (SatConstraint s : validateConstraint) {
//            if (s instanceof Spread)
            s.setContinuous(true);
        }
        cstrs.addAll(validateConstraint);
        try {
            plan = cra.solve(model, cstrs);
            if (plan == null) {
                sb.append(String.format("Model %2d. %d Continuous No solution\n", modelId, p));
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
//                        System.err.println("Does not satisfy continuous restriction");
                    }
                    s.setContinuous(continuous);
                }
            }
        } catch (SolverException e) {
            sb.append(String.format("Model %2d. Continuous: %s\n", modelId, e.getMessage()));
            return false;
        }
        if (satisfied) sb.append(String.format("%2d\t%2d\t%2d\t%3d\t%d\t%d\tC\n", modelId,
                p, vioTime[0], vioTime[1], vioTime[2], vioTime[3]));
        return satisfied;
    }

}
