package evaluation.demo;

import btrplace.model.*;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.constraint.CMaxOnlines;
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
    public void sumCapacity() throws SolverException {
        Model m = new DefaultModel();
        ShareableResource cpu = new ShareableResource("cpu", 4, 1);
        m.attach(cpu);
        Node n = m.newNode();
        Node n2 = m.newNode();
        m.getMapping().addOnlineNode(n);
        m.getMapping().addOnlineNode(n2);
        Assert.assertEquals(cpu.sumCapacities(m.getMapping().getOnlineNodes(), true), 8);
        Offline offline = new Offline(Collections.singleton(n));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(m, Collections.<SatConstraint>singleton(offline));
        Assert.assertEquals(cpu.sumCapacities(plan.getResult().getMapping().getOnlineNodes(), true), 4);

    }

    @Test
    public void spreadViolation() throws SolverException {
        Model model = new DefaultModel();
        ShareableResource cpu = new ShareableResource("cpu", 4, 1);
        model.attach(cpu);
        ArrayList<Node> ns = new ArrayList<>();
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
        Spread sp1 = new Spread(new HashSet<>(Arrays.asList(v0, v2)), continuous);
        Spread sp2 = new Spread(new HashSet<>(Arrays.asList(v1, v4)), continuous);
        Spread sp3 = new Spread(new HashSet<>(Arrays.asList(v3, v5)), continuous);
        Preserve preserve = new Preserve(Collections.singleton(v3), "cpu", 3);
        ArrayList<SatConstraint> ct = new ArrayList<>(Arrays.asList(sp1, sp2, sp3, preserve));
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
        ArrayList<Node> ns = new ArrayList<>();
        ArrayList<VM> vs = new ArrayList<>();
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

        Collection<VM> vms = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            VM e = model.newVM();
            vms.add(e);
            model.getMapping().addReadyVM(e);
        }
        Running run = new Running(vms);
        Overbook overbook = new Overbook(model.getNodes(), "cpu", 4);
        Collection<SatConstraint> VMconstraints = new ArrayList<>();
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

    @Test
    public void testLonely() throws SolverException {
        Model model = new DefaultModel();

        ShareableResource cpu = new ShareableResource("cpu", 8, 1);
        ShareableResource ram = new ShareableResource("ram", 32, 1);
        Overbook overcpu = new Overbook(model.getNodes(), "cpu", 4);
        Overbook overram = new Overbook(model.getNodes(), "ram", 4);
//        ShareableResource cpu = new ShareableResource("cpu", 32, 1);
//        ShareableResource ram = new ShareableResource("ram", 128, 1);
        model.attach(cpu);
        model.attach(ram);
        for (int i = 0; i < 8; i++) {
            Node n = model.newNode();
            model.getMapping().addOnlineNode(n);
        }
        for (int i = 0; i < 4; i++) {
            VM vm = model.newVM();
            model.getMapping().addReadyVM(vm);
        }
        HashSet<VM> vms = new HashSet<>();
        for (int i = 0; i < 18; i++) {
            VM vm = model.newVM();
            vms.add(vm);
            model.getMapping().addReadyVM(vm);
        }
        Set<VM> allVMs = model.getMapping().getAllVMs();
        Lonely lonely = new Lonely(vms);
        Running run = new Running(allVMs);
        SingleResourceCapacity srec = new SingleResourceCapacity(model.getNodes(), "cpu", 30);
        ArrayList<SatConstraint> constraints = new ArrayList<>();

        constraints.addAll(Arrays.asList(lonely, run, srec, overcpu, overram));
//        constraints.addAll(Arrays.asList(lonely, run, srec));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setTimeLimit(30);
        ReconfigurationPlan solve = cra.solve(model, constraints);
        Assert.assertNotNull(solve);
        System.out.println(solve.getResult().getMapping());
        constraints.remove(run);
        constraints.add(new Preserve(vms, "cpu", 8));
        constraints.add(new Preserve(vms, "ram", 7));
        for (SatConstraint s : constraints) {
            System.out.println(s);
        }
        ReconfigurationPlan plan = cra.solve(solve.getResult(), constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.getResult().getMapping());
        System.out.println(plan);
    }

    @Test
    public void testSplitAmong() {
        Model model = new DefaultModel();
        Collection<SatConstraint> constraints = new ArrayList<>();
        Collection<Collection<Node>> groups = new ArrayList<>();
        Collection<Collection<VM>> vms = new ArrayList<>();
        ShareableResource cpu = new ShareableResource("cpu", 32, 1);
        ShareableResource ram = new ShareableResource("ram", 128, 1);
        model.attach(cpu);
        model.attach(ram);
        Collection<Node> group1 = new ArrayList<>();
        Collection<Node> group2 = new ArrayList<>();
        for (int j = 0; j < 16; j++) {
            ArrayList<Node> ns = new ArrayList<>();
            for (int i = 0; i < 16; i++) {
                Node node = model.newNode();
                ns.add(node);
                model.getMapping().addOnlineNode(node);
            }
            if (j < 8) group1.addAll(ns);
            else group2.addAll(ns);
        }
        groups.add(group1);
        groups.add(group2);

        Application app1 = new Application(model);
        Application app2 = new Application(model);
        vms.add(app1.getAllVM());
        vms.add(app2.getAllVM());
        SplitAmong splitAmong = new SplitAmong(vms, groups);

        constraints.add(new Running(app1.getAllVM()));
        constraints.add(new Running(app2.getAllVM()));
        constraints.add(splitAmong);

        for (int i = 0; i < 450; i++) {
            Application app = new Application(model);
            constraints.add(new Running(app.getAllVM()));
        }
        SingleResourceCapacity SReC = new SingleResourceCapacity(model.getNodes(), "cpu", 30);
        SingleResourceCapacity SReC2 = new SingleResourceCapacity(model.getNodes(), "ram", 120);
        MaxOnline maxOnline = new MaxOnline(model.getNodes(), 250);
        constraints.add(SReC);
        constraints.add(SReC2);
        constraints.add(maxOnline);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        ReconfigurationPlan solve = null;
        try {
            solve = cra.solve(model, constraints);
        } catch (SolverException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(solve);
    }
}
