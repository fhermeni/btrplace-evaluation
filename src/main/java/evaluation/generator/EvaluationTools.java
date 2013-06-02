package evaluation.generator;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/21/13
 * Time: 2:20 PM
 */
public class EvaluationTools {
    private static final Logger log = LoggerFactory.getLogger("Tools");

    static public boolean satisfy(Model model, Collection<SatConstraint> co) {
        for (SatConstraint c : co) {
            if (!c.isSatisfied(model)) return false;
        }
        return true;
    }

    static public int getNumberOfDelayedAction(ReconfigurationPlan plan) {
        int i = 0;
        for (Action a : plan) {
            if (plan.getDirectDependencies(a).size() > 0) {
                i++;
            }
        }
        return i;
    }

    static public String analyze(ReconfigurationPlan d, ReconfigurationPlan c) {
        if (d == null) {
            return "Cannot compare: Discrete Plan is null";
        } else if (c == null)
            return "Cannot compare: Discrete Plan is null";

        StringBuilder sb = new StringBuilder("Compare DPlan and CPlan\n");
        sb.append(String.format("Duration:\t%d\t%d\n", d.getDuration(), c.getDuration()));
        sb.append(String.format("N. Action:\t%d\t%d\n", d.getSize(), c.getSize()));
        sb.append(String.format("N. Delay:\t%d\t%d\n", getNumberOfDelayedAction(d), getNumberOfDelayedAction(c)));
        return sb.toString();
    }

    static public ReconfigurationPlan solve(ChocoReconfigurationAlgorithm cra, Model model, Set<SatConstraint> constraints) {
        try {
            ReconfigurationPlan p = cra.solve(model, constraints);
            if (p != null) {
                log.info(cra.getSolvingStatistics().toString());
                return p;
            }
        } catch (SolverException e) {
            System.err.println("--- Solving using repair : " + cra.doRepair() + "; Error: " + e.getMessage());
            System.exit(-1);
        }
        return null;
    }

    static public Set<SatConstraint> toDiscrete(Set<SatConstraint> satConstraints) {
        for (SatConstraint c : satConstraints) {
            c.setContinuous(false);
        }
        return satConstraints;
    }

    static public Model prepareModel(Model model, Set<SatConstraint> satConstraints) {
        try {
            ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
            ReconfigurationPlan plan = cra.solve(model, satConstraints);
            return plan.getResult();

        } catch (SolverException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    static public int currentLoad(Model model) {
        Mapping mapping = model.getMapping();
        Set<Node> onlineNodes = mapping.getOnlineNodes();
        Set<VM> runningVMs = mapping.getRunningVMs();
        ShareableResource sr = (ShareableResource) model.getView("ShareableResource.cpu");
        int capacity = sr.sumCapacities(onlineNodes, true);
        int used = sr.sumConsumptions(runningVMs, true);
        return (100 * used) / capacity;
    }

    static public int capacity(Model model) {
        Mapping mapping = model.getMapping();
        Set<Node> onlineNodes = mapping.getOnlineNodes();
        ShareableResource sr = (ShareableResource) model.getView("ShareableResource.cpu");
        return sr.sumCapacities(onlineNodes, true);

    }
}
