package evaluation.generator;

import btrplace.model.*;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * User: Tu Huynh Dang
 * Date: 6/2/13
 * Time: 3:02 PM
 */
public class TestBtrPlace {

    @Test
    public void sumCapacity() {
        Model m = new DefaultModel();
        ShareableResource cpu = new ShareableResource("cpu", 4, 1);
        Node n = m.newNode();
        Node n2 = m.newNode();
        m.attach(cpu);
        Assert.assertEquals(cpu.sumCapacities(Arrays.asList(n, n2), true), 8);
    }

    @Test
    public void spreadViolation() throws SolverException {
        Model model = new DefaultModel();
        ShareableResource cpu = new ShareableResource("cpu", 4, 1);
        model.attach(cpu);
        ArrayList<Node> ns = new ArrayList<Node>();
        for (int i = 0; i < 3; i++) {
            Node node = model.newNode();
            ns.add(node);
            model.getMapping().addOnlineNode(node);
        }
        cpu.setCapacity(ns.get(2), 6);
        VM v0 = model.newVM();
        model.getMapping().addRunningVM(v0, ns.get(0));
        VM v1 = model.newVM();
        model.getMapping().addRunningVM(v1, ns.get(0));
        VM v2 = model.newVM();
        cpu.setConsumption(v2, 3);
        model.getMapping().addRunningVM(v2, ns.get(1));
        VM v3 = model.newVM();
        model.getMapping().addRunningVM(v3, ns.get(1));
        VM v4 = model.newVM();
        model.getMapping().addRunningVM(v4, ns.get(2));
        VM v5 = model.newVM();
        model.getMapping().addRunningVM(v5, ns.get(2));
        boolean continuous = false;
        boolean satisfied = true;
        Spread sp1 = new Spread(new HashSet<VM>(Arrays.asList(v0, v2)), continuous);
        Spread sp2 = new Spread(new HashSet<VM>(Arrays.asList(v1, v4)), continuous);
        Spread sp3 = new Spread(new HashSet<VM>(Arrays.asList(v3, v5)), continuous);
        Preserve preserve = new Preserve(Collections.singleton(v3), "cpu", 3);
        ArrayList<SatConstraint> ct = new ArrayList<SatConstraint>(Arrays.asList(sp1, sp2, sp3, preserve));
        while (satisfied) {
            ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
            ReconfigurationPlan plan = cra.solve(model, ct);
            System.out.println(plan);
            for (SatConstraint s : ct) {
                s.setContinuous(true);
                satisfied = s.isSatisfied(plan);
                s.setContinuous(false);
                if (!satisfied) {
                    System.out.println("Not Satisfy");
                    break;
                }
            }
        }
    }

    @Test
    public void testModelReference() throws SolverException {
        Model model = new DefaultModel();
        ShareableResource cpu = new ShareableResource("cpu", 4, 1);
        model.attach(cpu);
        ArrayList<Node> ns = new ArrayList<Node>();
        ArrayList<VM> vs = new ArrayList<VM>();
        for (int i = 0; i < 3; i++) {
            Node node = model.newNode();
            ns.add(node);
            VM vm = model.newVM();
            vs.add(vm);
            model.getMapping().addOnlineNode(node);
            model.getMapping().addReadyVM(vm);
        }
        Running o = new Running(vs);
        Set<SatConstraint> ctr = new HashSet<SatConstraint>(Collections.singleton(o));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, ctr);
        model = plan.getResult();
        System.out.println(model);
    }

    @Test
    public void testOverbook() throws SolverException {
        Model model = new DefaultModel();
        ShareableResource cpu = new ShareableResource("cpu", 8, 1);
        model.attach(cpu);
        model.getMapping().addOnlineNode(model.newNode());
        model.getMapping().addOnlineNode(model.newNode());

        Collection<VM> vms = new ArrayList<VM>();
        for (int i = 0; i < 64; i++) {
            VM e = model.newVM();
            vms.add(e);
            model.getMapping().addReadyVM(e);
        }
        Running run = new Running(vms);
        Overbook overbook = new Overbook(model.getNodes(), "cpu", 4);
        Collection<SatConstraint> VMconstraints = new ArrayList<SatConstraint>();
        VMconstraints.add(overbook);
        VMconstraints.add(run);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, VMconstraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.getResult().getMapping());
        Mapping mapping = plan.getResult().getMapping();
        Set<Node> onlineNodes = mapping.getOnlineNodes();
        Set<VM> runningVMs = mapping.getRunningVMs();
        ShareableResource sr = (ShareableResource) model.getView("ShareableResource.cpu");
        double capacity = sr.sumCapacities(onlineNodes, true) * overbook.getRatio();
        double used = sr.sumConsumptions(runningVMs, true);
        System.out.printf("%f %f %f", capacity, used, used / capacity * 100);
    }
}
