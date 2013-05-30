package evaluation.generator;

import btrplace.model.Model;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/7/13
 * Time: 2:36 PM
 */
public class HardwareFailures extends VariationType {

    private Set<Integer> offIds;
    private int node_size;
    private Random rand;

    public HardwareFailures(Model m, Set<SatConstraint> satConstraints) {
        super(m, satConstraints);
    }


    public ReconfigurationPlan run() {
        node_size = model.getMapping().getAllNodes().size();
        offIds = new HashSet<Integer>(node_size);
        rand = new Random(System.nanoTime() % 100000);
        constraints.add(shutdownRandomNode());
        ReconfigurationPlan plan = EvaluationTools.solve(cra, model, constraints);
        return plan;
    }


    public SatConstraint shutdownRandomNode() {

        Set<UUID> shutdownNodes = new HashSet<UUID>();
        for (SatConstraint c : constraints) {
            for (UUID vm : c.getInvolvedVMs()) {
                shutdownNodes.add(model.getMapping().getVMLocation(vm));
            }
            break;
        }

/*        int randomId;
        for (int i = 0; i < node_size; i++) {
            do {
                randomId = rand.nextInt(node_size);
            }
            while (offIds.contains(randomId));
            offIds.add(randomId);
            UUID n = new UUID(1, randomId);
            shutdownNodes = new HashSet<UUID>(Arrays.asList(n));
        }*/
        return new Offline(shutdownNodes);
    }
}



