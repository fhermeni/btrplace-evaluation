package evaluation.generator;

import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/30/13
 * Time: 10:52 AM
 */
public abstract class VariationType {
    public static ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    Model model;
    Set<SatConstraint> constraints;

    public VariationType(Model m, Set<SatConstraint> satConstraints) {
        model = m;
        constraints = satConstraints;
    }

    public abstract ReconfigurationPlan run();
}
