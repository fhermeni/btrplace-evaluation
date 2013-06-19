package evaluation.demo;

import btrplace.model.Node;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: TU HUYNH DANG
 * Date: 6/19/13
 * Time: 9:42 AM
 */
public class ServerFailures extends ReconfigurationScenario {

    Collection<Node> failedNodes;

    public ServerFailures(int id) {
        modelId = id;
        validateConstraint = new ArrayList<SatConstraint>();
        sb = new StringBuilder();
        failedNodes = new ArrayList<Node>();
        cra.setTimeLimit(300);
    }

    public static void main(String[] args) {
        ReconfigurationScenario instance = new ServerFailures(1);
        instance.sb.append("Model\tP\tS\tA\tSA\tSReC\tMO\tcontinuous\n");
        instance.run();
    }

    @Override
    public void run() {
        readData(modelId);
        int p = 3;
        List<Node> nodes = new ArrayList<Node>(model.getMapping().getAllNodes());
        int size = 10;
        Collections.shuffle(nodes);
        for (int i = 0; i < size; i++) {
            failedNodes.add(nodes.get(i));
        }
        reconfigure(p, false);
        reconfigure(p, true);
        System.out.println(sb.toString());
    }

    @Override
    boolean reconfigure(int p, boolean c) {
        int[] vioTime = new int[5];
        boolean satisfied = true;
        Collection<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        ReconfigurationPlan plan;
        cstrs.add(new Offline(failedNodes));
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
        sb.append(String.format("%d\t%d\t%d\t%d\t%d\t%d\t%d\t%b\n%s", modelId, p,
                vioTime[0], vioTime[1], vioTime[2], vioTime[3], vioTime[4], c, cra.getSolvingStatistics()));
        return satisfied;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
