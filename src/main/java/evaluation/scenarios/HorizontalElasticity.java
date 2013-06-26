package evaluation.scenarios;

import btrplace.model.VM;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.Spread;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.SolvingStatistics;
import evaluation.demo.Application;
import evaluation.generator.ConverterTools;

import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 6/18/13
 * Time: 5:06 PM
 */
public class HorizontalElasticity extends ReconfigurationScenario {

    Collection<VM> cloneVMs;

    public HorizontalElasticity(int id) {
        modelId = id;
        sb = new StringBuilder();
        cloneVMs = new ArrayList<>();
        cra.setTimeLimit(TIME_OUT);
        cra.doRepair(true);
    }

    public static void main(String[] args) {
        HorizontalElasticity he = new HorizontalElasticity(1);
        he.run();
    }

    @Override
    public void run() {
        readData(modelId);
        Collections.shuffle((new ArrayList<>(applications)));
        int p = 20;
        int size = applications.size() * p / 100;
        Iterator<Application> iterator = applications.iterator();
        while (size > 0 && iterator.hasNext()) {
            Application app = iterator.next();
            horizontalScale(app);
            size--;
        }
        reconfigure(p, false);
        reconfigure(p, true);
        System.out.print(this);
    }

    private void horizontalScale(Application app) {
        for (SatConstraint s : app.getConstraints()) {
            if (s instanceof Spread) {
                checkMap.remove(s);
            }
        }
        Collection<VM> tmp = new ArrayList<>();
        int id = model.getMapping().getAllVMs().size();
        for (int i = 0; i < app.getTier1().size(); i++) {
            VM vm = model.newVM(id++);
            model.getMapping().addReadyVM(vm);
            cloneVMs.add(vm);
            tmp.add(vm);
        }
        app.getTier1().addAll(tmp);
        tmp.clear();
        checkMap.put(new Spread(new HashSet<>(app.getTier1()), false), app.getId());
        for (int i = 0; i < app.getTier2().size(); i++) {
            VM vm = model.newVM(id++);
            ecu.setConsumption(vm, 4);
            ram.setConsumption(vm, 2);
            model.getMapping().addReadyVM(vm);
            tmp.add(vm);
            cloneVMs.add(vm);
        }
        app.getTier2().addAll(tmp);
        tmp.clear();
        checkMap.put(new Spread(new HashSet<>(app.getTier2()), false), app.getId());
        for (int i = 0; i < app.getTier3().size(); i++) {
            VM vm = model.newVM(id++);
            ram.setConsumption(vm, 4);
            model.getMapping().addReadyVM(vm);
            tmp.add(vm);
            cloneVMs.add(vm);
        }
        app.getTier3().addAll(tmp);
        tmp.clear();
        checkMap.put(new Spread(new HashSet<>(app.getTier3()), false), app.getId());
    }

    @Override
    boolean reconfigure(int p, boolean c) {
        int DCconstraint[] = new int[2];
        ArrayList<ArrayList<Integer>> violatedConstraints = new ArrayList<>();
        HashSet<Integer> affectedApps = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            violatedConstraints.add(new ArrayList<Integer>());
        }
        boolean satisfied = true;
        Collection<SatConstraint> cstrs = new ArrayList<>();
        ReconfigurationPlan plan;
        cstrs.add(new Running(cloneVMs));


        if (c) {
            for (SatConstraint s : validateConstraint) {
                s.setContinuous(true);
            }
        }

        cstrs.addAll(validateConstraint);
        try {
            plan = cra.solve(model, cstrs);
            if (plan == null) {
                sb.append(String.format("Model %d\t %b \t No solution\n", modelId, c));
                return false;
            } else {
                checkSatisfaction(plan, violatedConstraints, DCconstraint, affectedApps);
            }
        } catch (SolverException e) {
            sb.append(String.format("Model %d.\t%b\t%s\n", modelId, c, e.getMessage()));
            return false;
        }
        String path = System.getProperty("user.home") + System.getProperty("file.separator") + "plan"
                + System.getProperty("file.separator") + "he" + System.getProperty("file.separator");

        ConverterTools.planToFile(plan, String.format("%s%d%b", path, modelId, c));
        sb.append(String.format("%-2d\t%b\t%-3d\t%-2d\t%d\t%d\t%d\t%d\t%d\t", modelId, c, p,
                violatedConstraints.get(0).size(), violatedConstraints.get(1).size(), violatedConstraints.get(2).size(),
                DCconstraint[0], DCconstraint[1], affectedApps.size()));
        float[] load = currentLoad(model);
        sb.append(String.format("%f\t%f\t", load[0], load[1]));
        load = currentLoad(plan.getResult());
        sb.append(String.format("%f\t%f\t", load[0], load[1]));
        SolvingStatistics statistics = cra.getSolvingStatistics();
        sb.append(String.format("%d\t%d\t%d\n", statistics.getSolvingDuration(), plan.getDuration(), plan.getSize()));
        return satisfied;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
