package evaluation.demo;

import btrplace.json.JSONConverterException;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;
import evaluation.generator.ConverterTools;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 6/25/13
 * Time: 1:02 PM
 */
public class PlanReader {
    String planFile;
    String modelFile;
    String appFile;
    Map<SatConstraint, Integer> checkMap;

    public PlanReader(String planFile, String modelFile, String appFile) {
        this.planFile = planFile;
        this.modelFile = modelFile;
        this.appFile = appFile;

    }

    private void readModel() {
       Model model = ConverterTools.getModelFromFile(modelFile);
//        validateConstraint = ConverterTools.getConstraints(model, path + "constraints" + id + ".json");
        List<Application> applications = ConverterTools.getApplicationsFromFile(model, appFile);
        checkMap = new HashMap<>(1200);
        for (Application a : applications) {
            for (SatConstraint s : a.getConstraints()) {
                checkMap.put(s, a.getId());
            }
        }
        Set<Node> allNodes = model.getMapping().getAllNodes();
        SingleResourceCapacity SReC = new SingleResourceCapacity(allNodes, "ecu", 60, false);
        SingleResourceCapacity SReC2 = new SingleResourceCapacity(allNodes, "ram", 120, false);
        MaxOnline maxOnline = new MaxOnline(allNodes, 240, false);
        checkMap.put(SReC, 1000);
        checkMap.put(SReC2, 1000);
        checkMap.put(maxOnline, 1000);
    }

    /*public ReconfigurationPlan readPlan() throws IOException, JSONConverterException {
        ReconfigurationPlanConverter converter = new ReconfigurationPlanConverter();
        ReconfigurationPlan plan = null;
        try {
            plan = converter.fromJSON(new File(planFile));
            double[] act_types = countActions(plan);
            System.out.printf("%f\t%f\t%f\n", act_types[0], act_types[1], act_types[2]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plan;
    }           */

    private void levelDependency(ReconfigurationPlan plan) {
        ArrayList<ArrayList<Action>> d = new ArrayList<>();
        for (int i = 0; i <= plan.getDuration(); i++) {
            d.add(new ArrayList<Action>());
        }
        for (Action a : plan) {
            int level = a.getStart();
            d.get(level).add(a);
        }

        for (int l = 0; l < d.size(); l++) {
            System.out.printf("%d\t", d.get(l).size());
        }
        System.out.println();
    }

    private double calculateAverage(ReconfigurationPlan plan) {

        ArrayList<Integer> marks = new ArrayList<>();
        for (Node n : plan.getResult().getMapping().getAllNodes()) {
            int size = plan.getResult().getMapping().getRunningVMs(n).size();
            marks.add(size);
        }

        Integer sum = 0;
        if (!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }

    public static int[] countActions(ReconfigurationPlan plan) {
        int[] count = new int[5];
        for (Action a : plan) {
            if (a instanceof BootVM) count[0]++;
            else if (a instanceof MigrateVM) count[1]++;
            else if (a instanceof Allocate) count[2]++;
            else if (a instanceof BootNode) count[3]++;
            else if (a instanceof ShutdownNode) count[4]++;
        }
        return count;
    }

    private int[] countBootShutdown(ReconfigurationPlan plan) {
        int[] count = new int[2];
        for (Action a : plan) {
            if (a instanceof BootNode) count[0]++;
            else if (a instanceof ShutdownNode) count[1]++;
        }
        return count;
    }

    public int[] countViolation(ReconfigurationPlan plan) {
        HashSet<Integer> constr[] = new HashSet[3];
        HashSet<Integer> apps = new HashSet<>();
        int violated[] = new int[6];
        for (int i = 0; i < 3; i++) {
            constr[i] = new HashSet<>();
        }

        for (SatConstraint s : checkMap.keySet()) {
            Integer appId = checkMap.get(s);
            boolean continuous = s.isContinuous();
            if (!continuous) s.setContinuous(true);
            if (!s.isSatisfied(plan)) {
                if (s instanceof Spread) {
                    constr[0].add(appId);
                    apps.add(appId);
                } else if (s instanceof Among) {
                    constr[1].add(appId);
                    apps.add(appId);
                } else if (s instanceof SplitAmong) {
                    constr[2].add(appId);
                    apps.add(appId);
                } else if (s instanceof SingleResourceCapacity) {
                    violated[3]++;
                } else if (s instanceof MaxOnline) {
                    violated[4]++;
                }
            }
            s.setContinuous(continuous);
        }
        Set<Integer> set = new HashSet<>(constr[1]);
        if (set.size() < constr[1].size()) {
            System.out.println("Duplicate");
        }

        for (int i = 0; i < 3; i++) {
            violated[i] = constr[i].size();
        }
        violated[5] = apps.size();
        return violated;
    }

}
