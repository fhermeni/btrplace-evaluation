package evaluation.scenarios;

import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.SolvingStatistics;

import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 6/19/13
 * Time: 9:42 AM
 */
public class ServerFailures extends ReconfigurationScenario {

    Collection<Node> failedNodes;
    Collection<VM> restartVMs;

    public ServerFailures(int id) {
        modelId = id;
        validateConstraint = new ArrayList<>();
        sb = new StringBuilder();
        failedNodes = new ArrayList<>();
        restartVMs = new ArrayList<>();
        cra.setTimeLimit(TIME_OUT);
        cra.doRepair(true);
    }

    public static void main(String[] args) {
        ReconfigurationScenario instance = new ServerFailures(1);
        instance.run();
    }

    @Override
    public void run() {
        readData(modelId);
        int p = 5;
        List<Node> nodes = new ArrayList<>(model.getMapping().getAllNodes());
        int size = p * nodes.size() / 100;
        Collections.shuffle(nodes);
        for (int i = 0; i < size; i++) {
            Node e = nodes.get(i);
            Set<VM> runningVMs = model.getMapping().getRunningVMs(e);
            restartVMs.addAll(runningVMs);
            model.getMapping().clearNode(e);
            if (!model.getMapping().addOfflineNode(e)) {
                System.err.println("Remove node failed");
                System.exit(0);
            } else failedNodes.add(e);
        }
        for (Node n : failedNodes) {
            if (model.getMapping().getOnlineNodes().contains(n)) {
                System.err.println("Still run");
                System.exit(0);
            }
        }
        for (VM vm : restartVMs) {
            model.getMapping().addReadyVM(vm);
        }
        reconfigure(size, false);
        reconfigure(size, true);

        System.out.print(sb.toString());
    }

    @Override
    boolean reconfigure(int p, boolean c) {
        int[] vioTime = new int[5];
        boolean satisfied = true;
        Collection<SatConstraint> constraints = new ArrayList<>();
        ReconfigurationPlan plan;
        constraints.add(new Running(restartVMs));
        constraints.add(new Offline(failedNodes));
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
                for (Node n : failedNodes) {
                    if (plan.getResult().getMapping().getOnlineNodes().contains(n)) {
                        System.err.println("Failed servers run again");
                    }
                }

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
