package evaluation.demo;

import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.Among;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.Spread;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import evaluation.generator.EvaluationTools;
import evaluation.generator.RUBBoS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Tu Huynh Dang
 * Date: 6/2/13
 * Time: 8:51 AM
 */
public class Evaluator {
    static ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    private Set<SatConstraint> constraintSet = new HashSet<SatConstraint>();
    private Model model;
    private ArrayList<Collection<Node>> ssNode;

    public Evaluator(String file) {
        try {
            ModelConverter converter = new ModelConverter();
            model = converter.fromJSON(new File(file));

            ssNode = new ArrayList<Collection<Node>>(4);
            for (int i = 0; i < 4; i++) {
                Set<Node> set = new HashSet<Node>();
                ssNode.add(set);
            }
            for (Node node : model.getMapping().getAllNodes()) {
                ssNode.get(node.id() % 4).add(node);
            }
        } catch (JSONConverterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public boolean checkPlan(ReconfigurationPlan plan, Set<SatConstraint> co) {
        for (SatConstraint c : co) {
            if (!c.isSatisfied(plan)) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Evaluator e = new Evaluator(args[0]);
        RUBBoS.setModel(e.getModel());
        int count = 1;
        boolean c;
        do {
            System.out.println("Run " + count++ + " RUBBoS");
            RUBBoS rubBoS = new RUBBoS(1, 4, 16, 16);
            c = e.runRUBBoS(rubBoS);
            System.out.println("Current Load: " + EvaluationTools.currentLoad( e.getModel()));
//            System.out.println(e.getModel().getMapping());
        }
        while (c);

    }

    public boolean runRUBBoS(RUBBoS rubBoS) {

        Set<SatConstraint> constraints = new HashSet<SatConstraint>();

        for (Set<VM> t : rubBoS.getTiers()) {
            for (VM vm : t) {
                model.getMapping().addReadyVM(vm);
            }
        }

        for (Set<VM> t : rubBoS.getTiers()) {
            Running r = new Running(t);
            Spread s = new Spread(t, false);
            constraints.add(r);
            constraints.add(s);
        }

        Among among = new Among(rubBoS.getTier4(),(Collection<Collection<Node>>) ssNode);
        constraints.add(among);


        try {
            ReconfigurationPlan plan = cra.solve(model, constraints);
            cra.setTimeLimit(20);
            if (plan != null) {
                System.out.println("Satisfy:" + checkPlan(plan, constraintSet));
                model = plan.getResult();
                return true;
            }
        } catch (SolverException e1) {
            System.err.println(e1.getMessage());
            return false;
        }
        return false;
    }

    public Model getModel() {
        return model;
    }

}
