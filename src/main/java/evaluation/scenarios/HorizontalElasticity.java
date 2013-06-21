package evaluation.scenarios;

import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.SolvingStatistics;
import evaluation.demo.Application;

import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 6/18/13
 * Time: 5:06 PM
 */
public class HorizontalElasticity extends ReconfigurationScenario {

    Collection<SatConstraint> validate;
    Collection<VM> cloneVMs;

    public HorizontalElasticity(int id) {
        modelId = id;
        validateConstraint = new ArrayList<>();
        sb = new StringBuilder();
        validate = new ArrayList<>();
        cloneVMs = new ArrayList<>();
        cra.setTimeLimit(TIME_OUT);
    }

    public static void main(String[] args) {
        HorizontalElasticity he = new HorizontalElasticity(1);
        he.sb.append("Model\tP\tS\tA\tSA\tSReC\tMO\tcontinuous\n");
        he.run();
    }

    @Override
    boolean reconfigure(int p, boolean c) {
        int[] vioTime = new int[5];
        boolean satisfied = true;
        Collection<SatConstraint> cstrs = new ArrayList<>();
        ReconfigurationPlan plan;
        cstrs.add(new Running(cloneVMs));
        validateConstraint.addAll(validate);
        if (c) {
            for (SatConstraint s : validateConstraint) {
                s.setContinuous(true);
            }
        }
        cstrs.addAll(validateConstraint);
        try {
            plan = cra.solve(model, cstrs);
            if (plan == null) {
                sb.append(String.format("Model %d. %d %b No solution\n", modelId, p, c));
                return false;
            } else {
                for (SatConstraint s : validateConstraint) {
                    boolean continuous = s.isContinuous();
                    if (!continuous) s.setContinuous(true);
                    if (!s.isSatisfied(plan)) {
                        satisfied = false;
                        if (s instanceof Spread) {
                            vioTime[0]++;
                        } else if (s instanceof Among) {
                            vioTime[1]++;
                        } else if (s instanceof SingleResourceCapacity) {
                            vioTime[2]++;
                        } else if (s instanceof MaxOnline) {
                            vioTime[3]++;
                        }
                    }
                    s.setContinuous(continuous);
                }
            }
        } catch (SolverException e) {
            sb.append(String.format("Model %d. %b: %s\n", modelId, c, e.getMessage()));
            return false;
        }
        validateConstraint.removeAll(validate);
        sb.append(String.format("%-2d\t%-3d\t%-2d\t%d\t%d\t%d\t%d\t%b\n", modelId, p,
                vioTime[0], vioTime[1], vioTime[2], vioTime[3], vioTime[4], c));
        float[] load = currentLoad(model);
        sb.append(String.format("%f\t%f\t", load[0], load[1]));
        load = currentLoad(plan.getResult());
        sb.append(String.format("%f\t%f\t", load[0], load[1]));
        SolvingStatistics statistics = cra.getSolvingStatistics();
        sb.append(String.format("%d\t%d\t%d\n", statistics.getSolvingDuration(), plan.getDuration(), plan.getSize()));
        return satisfied;
    }

    private void horizontalScale(Application app) {
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
        validate.add(new Spread(new HashSet<>(app.getTier1())));
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
        validate.add(new Spread(new HashSet<>(app.getTier2())));
        for (int i = 0; i < app.getTier3().size(); i++) {
            VM vm = model.newVM(id++);
            ram.setConsumption(vm, 4);
            model.getMapping().addReadyVM(vm);
            tmp.add(vm);
            cloneVMs.add(vm);
        }
        app.getTier3().addAll(tmp);
        tmp.clear();
        validate.add(new Spread(new HashSet<>(app.getTier1())));
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
        System.out.println(this);
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
