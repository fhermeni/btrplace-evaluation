package evaluation.scenarios;

import btrplace.json.JSONConverterException;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 6/19/13
 * Time: 9:42 AM
 */
public class ServerFailures extends ReconfigurationScenario {

    Collection<Node> failedNodes;
    Collection<VM> restartVMs;

    public ServerFailures(String in, String out) throws ParseException, IOException, JSONConverterException {
        super(in, out);
        failedNodes = new ArrayList<>();
        restartVMs = new ArrayList<>();
        rp_type = "sf";
    }

    @Override
    public void run() {
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
        if (findContinuous)
            reconfigure(size, true);
        else
            reconfigure(size, false);
        System.out.print(sb.toString());
    }

    @Override
    boolean reconfigure(int p, boolean c) {
        int DCconstraint[] = new int[2];
        HashSet<Integer>[] violatedConstraints = new HashSet[3];
        HashSet<Integer> affectedApps = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            violatedConstraints[i] = new HashSet<>();
        }
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
                sb.append(String.format("%d\tNo solution\n", modelId));
                return false;
            } else {
                for (Node n : failedNodes) {
                    if (plan.getResult().getMapping().getOnlineNodes().contains(n)) {
                        System.err.println("Failed servers run again");
                    }
                }
                checkSatisfaction(plan, violatedConstraints, DCconstraint, affectedApps);
            }
            result(plan, violatedConstraints, DCconstraint, affectedApps);
        } catch (SolverException e) {
            sb.append(String.format("%d\t%s\n", modelId, e.getMessage()));
            return false;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
        return satisfied;
    }
}
