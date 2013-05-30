package evaluation.generator;

import btrplace.model.Model;
import btrplace.model.constraint.Preserve;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;

import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 5/30/13
 * Time: 10:14 AM
 */
public class CpuPeak extends VariationType {
    private int percent;

    public CpuPeak(Model m, Set<SatConstraint> satConstraints) {
        super(m, satConstraints);
    }

    public ReconfigurationPlan run() {

        constraints.addAll(preserveForInvolveVMs());
        ReconfigurationPlan plan = EvaluationTools.solve(cra, model, constraints);
        return plan;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    private Set<SatConstraint> preserveForInvolveVMs() {
        int capacity = EvaluationTools.capacity(model);
        int size = (capacity * percent) / 100;

        ShareableResource sr = (ShareableResource) model.getView("ShareableResource.cpu");
        Set<SatConstraint> additional_constraint = new HashSet<SatConstraint>();
        for (SatConstraint c : constraints) {
            for (UUID vm : c.getInvolvedVMs()) {
                additional_constraint.add(new Preserve(new HashSet<UUID>(Collections.singleton(vm)), "cpu", sr.get(vm) + 1));
                if (size-- <= 0) break;
            }
        }
        ArrayList<UUID> list = new ArrayList<UUID>(model.getMapping().getAllVMs());
        while (size > 0) {
            Random rand = new Random(System.nanoTime() % 100000);
            int i = rand.nextInt(model.getMapping().getRunningVMs().size());
            UUID vm = list.get(i);
            additional_constraint.add(new Preserve(new HashSet<UUID>(Collections.singleton(vm)), "cpu", sr.get(vm) + 1));
            size--;
        }
        return additional_constraint;
    }
}
