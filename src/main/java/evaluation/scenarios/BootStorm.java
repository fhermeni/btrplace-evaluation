package evaluation.scenarios;

import btrplace.model.VM;
import btrplace.model.constraint.Preserve;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.SolvingStatistics;
import evaluation.generator.ConverterTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * User: TU HUYNH DANG
 * Date: 6/19/13
 * Time: 10:05 AM
 */
public class BootStorm extends ReconfigurationScenario {

    public BootStorm(int id) {
        modelId = id;
        sb = new StringBuilder();
        cra.setTimeLimit(TIME_OUT);
        cra.doRepair(true);
    }

    public static void main(String[] args) {
        ReconfigurationScenario instance = new BootStorm(1);
        instance.run();
    }

    @Override
    public void run() {
        readData(modelId);
        int p = 400;
        reconfigure(p, false);
        reconfigure(p, true);
        System.out.print(this);
    }

    @Override
    public boolean reconfigure(int p, boolean c) {
        int DCconstraint[] = new int[2];
        ArrayList<ArrayList<Integer>> violatedApp = new ArrayList<>();
        HashSet<Integer> affectedApps = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            violatedApp.add(new ArrayList<Integer>());
        }
        boolean satisfied = true;
        int currentVmId = model.getMapping().getAllVMs().size();
        Collection<SatConstraint> cstrs = new ArrayList<>();
        ReconfigurationPlan plan;
        Collection<VM> bootVMS1 = new ArrayList<>();
        Collection<VM> bootVMS2 = new ArrayList<>();
        Collection<VM> bootVMS3 = new ArrayList<>();
        for (int i = 0; i < p; i++) {
            VM vm = model.newVM(currentVmId++);
            model.getMapping().addReadyVM(vm);
            if (i % 3 == 0) bootVMS1.add(vm);
            else if (i % 3 == 1) bootVMS2.add(vm);
            else bootVMS3.add(vm);
        }
        cstrs.add(new Running(bootVMS1));
        cstrs.add(new Running(bootVMS2));
        cstrs.add(new Running(bootVMS3));
        cstrs.add(new Preserve(bootVMS1, "ecu", 2));
        cstrs.add(new Preserve(bootVMS1, "ram", 4));
        cstrs.add(new Preserve(bootVMS2, "ecu", 14));
        cstrs.add(new Preserve(bootVMS2, "ram", 7));
        cstrs.add(new Preserve(bootVMS3, "ecu", 4));
        cstrs.add(new Preserve(bootVMS3, "ram", 17));

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
                checkSatisfaction(plan, violatedApp, DCconstraint, affectedApps);
            }
        } catch (SolverException e) {
            sb.append(String.format("Model %d.\t%b\t%s\n", modelId, c, e.getMessage()));
            return false;
        }
        String path = System.getProperty("user.home") + System.getProperty("file.separator") + "plan"
                + System.getProperty("file.separator") + "bs" + System.getProperty("file.separator");

        ConverterTools.planToFile(plan, String.format("%s%d%b", path, modelId, c));

        sb.append(String.format("%-2d\t%b\t%-3d\t%-2d\t%d\t%d\t%d\t%d\t%d\t", modelId, c, p,
                violatedApp.get(0).size(), violatedApp.get(1).size(), violatedApp.get(2).size(),
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
