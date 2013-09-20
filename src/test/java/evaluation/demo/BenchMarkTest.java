package evaluation.demo;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.Among;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.plan.event.MigrateVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 6/4/13
 * Time: 9:08 AM
 */
public class BenchMarkTest {

    /*
    This test is a corner case of among that is not covered yet.
    @Test
    public void testContinuousAmong() throws Exception {
        Model model = new DefaultModel();
        VM vm0 = model.newVM(0);
        VM vm1 = model.newVM(1);
        final Node node0 = model.newNode();
        final Node node1 = model.newNode();
        model.getMapping().addOnlineNode(node0);
        model.getMapping().addOnlineNode(node1);
        model.getMapping().addRunningVM(vm0, node0);
        model.getMapping().addReadyVM(vm1);
        Collection<VM> lvm = new ArrayList<>(Arrays.asList(vm0, vm1));
        final Collection<Node> n1 = Collections.singleton(node0);
        Collection<Collection<Node>> lnode = new ArrayList<Collection<Node>>() {{
            add(n1);
            add(Collections.singleton(node1));
        }};
        Offline offline = new Offline(n1);
        Among sa = new Among(lvm, lnode, false);
        Running run = new Running(Arrays.asList(vm1));
        Assert.assertTrue(sa.isSatisfied(model));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, Arrays.asList(sa, offline, run));
        Assert.assertNotNull(plan);
        Assert.assertTrue(sa.isSatisfied(plan));
    }      */


    @Test
    public void test1() throws Exception {
        Model model = new DefaultModel();
        ArrayList<Node> sn1 = new ArrayList<Node>();
        ArrayList<Node> sn2 = new ArrayList<Node>();
        ShareableResource rc = new ShareableResource("cpu", 8, 1);
        model.attach(rc);

        for (int i = 0; i < 8; i++) {
            Node e = model.newNode();
            model.getMapping().addOnlineNode(e);
            if (i < 4) {
                sn1.add(e);
            } else sn2.add(e);
        }

        Random r = new Random();
        ArrayList<VM> vms = new ArrayList<VM>();
        for (int i = 0; i < 3; i++) {
            int i1 = r.nextInt(2) + 1;

            VM v = model.newVM();
            rc.setConsumption(v, i1 * 2);
            model.getMapping().addRunningVM(v, sn1.get(i));
            vms.add(v);
        }

        ArrayList<Collection<Node>> parts = new ArrayList<Collection<Node>>();
        parts.add(sn1);
        parts.add(sn2);
        Among among = new Among(vms, parts, true);
        Collection<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(among);

        boolean c = true;
        int count = 0;
        StringBuilder sb = new StringBuilder();
        while (c) {
            int i = r.nextInt(2) + 1;
            sb.append(String.format("Demand: %d\t", i));
            VM vm = model.newVM();
            rc.setConsumption(vm, i);
            vms.add(vm);
            model.getMapping().addReadyVM(vm);
            Running run = new Running(Collections.singleton(vm));
            constraints.add(run);
            c = solve(model, constraints, among);
            count++;
        }
        System.out.println(sb.toString() + "\nTotal: " + count);
    }

    public boolean solve(Model m, Collection<SatConstraint> constraints, SatConstraint check) throws SolverException {
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(m, constraints);
        if (plan != null) {
            for (Action a : plan) {
                if (a instanceof MigrateVM) {
                    System.out.println(a);
                }
            }
            System.out.println(plan);
            return check.isSatisfied(plan);

        } else {
            System.err.println("No plan found");
            return false;
        }
    }
}
