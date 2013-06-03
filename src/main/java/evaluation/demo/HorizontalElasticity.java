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
import evaluation.generator.ConverterTools;
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
public class HorizontalElasticity {
    static ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    private static final int CRA_TIMEOUT = 20;
    private Model model;
    private ArrayList<Collection<Node>> ssNode;

    public HorizontalElasticity(String file) {
        try {
            ModelConverter converter = new ModelConverter();
            model = converter.fromJSON(new File(file));

            // Create a set of 4 sets of nodes
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

    public static void main(String[] args) {
        HorizontalElasticity e = new HorizontalElasticity(args[0]);
        Boolean cont = true;
        RUBBoS.setInfrastructure(e.getModel());
        ArrayList<RUBBoS> apps = new ArrayList<RUBBoS>();


        //-------------- For a 0% load model to 70% load------------------------------------
        int count = 1;
        boolean c;
        int utilization;
        do {
            System.out.println("Run " + count++ + " RUBBoS");
            // Create a new application with 4 tiers with specified numbers of replication
            RUBBoS rubBoS = new RUBBoS(1, 4, 16, 16);
            apps.add(rubBoS);
            c = e.runRUBBoS(rubBoS, cont);
            utilization = EvaluationTools.currentLoad(e.getModel());
        }
        while (utilization < 70 && c);
//        modelToFile(e.getModel(), "Model.json");
        //----------------------------------------------------------------------------------


        System.out.println(e.getModel().getMapping());
        System.out.println(EvaluationTools.currentLoad(e.getModel()));
        ReconfigurationPlan plan = e.increaseLoad(25, apps, cont);
        System.out.println(EvaluationTools.currentLoad(e.getModel()));
//         planToFile(plan, "discretePlan.json");
        ConverterTools.planToFile(plan, "continuousPlan.json");


    }


    private ReconfigurationPlan increaseLoad(int p, Collection<RUBBoS> appList, Boolean cont) {

        ArrayList<SatConstraint> spreadVms = new ArrayList<SatConstraint>();
        Collection<VM> elasticVMs = new ArrayList<VM>();
        for (RUBBoS app : appList) {
            for (Set<VM> t : app.getTiers()) {
                int size = t.size();
                int inc = (size * p) / 100;
                while (inc > 0) {
                    VM vm = RUBBoS.getModel().newVM();
                    model.getMapping().addReadyVM(vm);
                    elasticVMs.add(vm);
                    t.add(vm);
                    inc--;
                }
                Spread s = new Spread(t, cont);
                spreadVms.add(s);
            }
        }

        Running run = new Running(elasticVMs);
        spreadVms.add(run);
        cra.setTimeLimit(CRA_TIMEOUT);
        try {
            ReconfigurationPlan plan = cra.solve(model, spreadVms);
            if (plan != null) {
                System.out.println(cra.getSolvingStatistics());
                model = plan.getResult();
                return plan;
            }

        } catch (SolverException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    public boolean runRUBBoS(RUBBoS rubBoS, Boolean cont) {

        Set<SatConstraint> constraints = new HashSet<SatConstraint>();

        for (Set<VM> t : rubBoS.getTiers()) {
            for (VM vm : t) {
                model.getMapping().addReadyVM(vm);
            }
        }
        // All replicas of each tier are hosted by different nodes
        for (Set<VM> t : rubBoS.getTiers()) {
            Running r = new Running(t);
            Spread s = new Spread(t, cont);
            constraints.add(r);
            constraints.add(s);
        }

        // Replicas of tier-4 (Data Nodes) are hosted among (for example) fastest connection.
        Among among = new Among(rubBoS.getTier4(), ssNode);
        constraints.add(among);


        try {
            ReconfigurationPlan plan = cra.solve(model, constraints);
            cra.setTimeLimit(CRA_TIMEOUT);
            if (plan != null) {
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
