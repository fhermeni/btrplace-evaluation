package evaluation.demo;

import btrplace.model.*;
import btrplace.model.constraint.Among;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: Tu Huynh Dang
 * Date: 6/11/13
 * Time: 10:14 PM
 */
public class LoadPeak {
    static Model model = new DefaultModel();

    public static void main(String[] args) throws SolverException {
        ShareableResource cpu = new ShareableResource("cpu", 32, 1);
        model.attach(cpu);

        ArrayList<Collection<Node>> nss = new ArrayList<Collection<Node>>();
        for (int j = 0; j < 4; j++) {
            ArrayList<Node> ns = new ArrayList<Node>();
            for (int i = 0; i < 30; i++) {
                Node node = model.newNode();
                if(i%3 == 0) ns.add(node);
                model.getMapping().addOnlineNode(node);
            }
            nss.add(ns);
        }
        Model clone = model.clone();
        boolean cont;
        int count = 0;
        do {
            Application app = new Application(model);
            cont = runApp(app, nss);
            Assert.assertNotEquals(model, clone);
            System.out.println(String.format("Runned %d applications", ++count));
        }
        while (cont) ;
    }

    public static boolean runApp(Application app, Collection<Collection<Node>> nodeSets) throws SolverException {
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setTimeLimit(10);
        Collection<SatConstraint> cstrs = app.initConstraints();
        Among among = new Among(app.tier3, nodeSets);
        cstrs.add(among);
        ReconfigurationPlan plan =  cra.solve(model, cstrs);;
        if(plan!=null) {
            model = plan.getResult();
            return true;
        }
        return false;
    }

}
