package evaluation.generator;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
        return EvaluationTools.solve(cra, model, constraints);
    }


    public SatConstraint shutdownRandomNode() {

        Set<Node> shutdownNodes = new HashSet<Node>();
        for (SatConstraint c : constraints) {
            for (VM vm : c.getInvolvedVMs()) {
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



