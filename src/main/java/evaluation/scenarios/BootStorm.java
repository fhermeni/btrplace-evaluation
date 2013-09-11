package evaluation.scenarios;

import btrplace.json.JSONConverterException;
import btrplace.model.VM;
import btrplace.model.constraint.Preserve;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * User: TU HUYNH DANG
 * Date: 6/19/13
 * Time: 10:05 AM
 */
public class BootStorm extends ReconfigurationScenario {

    public BootStorm(String in, String out) throws ParseException, IOException, JSONConverterException {
        super(in, out);
        rp_type = 3;
    }

    @Override
    public void run() {
        int p = 400;
        if (findContinuous)
            reconfigure(p, true);
        else
            reconfigure(p, false);
    }

    @Override
    public boolean reconfigure(int p, boolean c) {
        int DCconstraint[] = new int[2];
        HashSet<Integer> violatedConstraints[] = new HashSet[3];
        HashSet<Integer> affectedApps = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            violatedConstraints[i] = new HashSet<>();
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
                reportIssue(true);
                return false;
            } else {
                checkSatisfaction(plan, violatedConstraints, DCconstraint, affectedApps);
            }
            result(plan, violatedConstraints, DCconstraint, affectedApps);
        } catch (SolverException e) {
            reportIssue(false);
            return false;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
        return satisfied;
    }
}
