package evaluation.scenarios;

import btrplace.plan.ReconfigurationPlan;
import evaluation.nouse.PlanReader;

/**
 * User: TU HUYNH DANG
 * Date: 7/10/13
 * Time: 10:06 AM
 */
public class ConstraintViolation extends ReconfigurationScenario {

    public ConstraintViolation(String mfile, String appFile, String out) {
        super(mfile, appFile, out);
    }

    @Override
    boolean reconfigure(int p, boolean c) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void run() {
        String file = "/user/hdang/home/newEvaluation/plan/storm";
        PlanReader pr = new PlanReader(String.format("%s/plan%dfalse.json", file, modelId));
        ReconfigurationPlan plan = pr.read();
        if (plan != null) {
            readData();
            int[] v = countViolation(plan);
            System.out.printf("%d\t%d\t%d\t%d\t%d\t%d\t%d\n", modelId, v[0], v[1], v[2], v[3], v[4], v[5]);
        }
    }

    public static void main(String[] args) {
        ReconfigurationScenario instance = new ConstraintViolation(args[0], args[1], args[2]);
        instance.run();
    }
}
