package evaluation.scenarios;

import btrplace.json.JSONConverterException;
import btrplace.model.VM;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.Spread;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import evaluation.demo.Application;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 6/18/13
 * Time: 5:06 PM
 */
public class HorizontalElasticity extends ReconfigurationScenario {

    Collection<VM> cloneVMs;

    public HorizontalElasticity(String in, String out) throws ParseException, IOException, JSONConverterException  {
        super(in, out);
        cloneVMs = new ArrayList<>();
        rp_type = "he";
    }

    @Override
    public void run() {
        Collections.shuffle((new ArrayList<>(applications)));
        int p = 25;
        int size = applications.size() * p / 100;
        Iterator<Application> iterator = applications.iterator();
        while (size > 0 && iterator.hasNext()) {
            Application app = iterator.next();
            horizontalScale(app);
            size--;
        }
        if (findContinuous)
            reconfigure(p, true);
        else
            reconfigure(p, false);
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
        HashSet<Integer>[] violatedConstraints = new HashSet[3];
        HashSet<Integer> affectedApps = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            violatedConstraints[i] = new HashSet<>();
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
                sb.append(String.format("%d\tNo solution\n", modelId));
                return false;
            } else {
                checkSatisfaction(plan, violatedConstraints, DCconstraint, affectedApps);
            }
            result(plan, violatedConstraints, DCconstraint, affectedApps);
        } catch (SolverException e) {
            sb.append(String.format("%d\t%s\n", modelId, e.getMessage()));
            return false;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
        return satisfied;
    }
}
