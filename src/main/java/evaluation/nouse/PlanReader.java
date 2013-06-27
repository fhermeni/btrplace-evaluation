package evaluation.nouse;

import btrplace.json.JSONConverterException;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.model.Node;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * User: TU HUYNH DANG
 * Date: 6/25/13
 * Time: 1:02 PM
 */
public class PlanReader {
    String filename;

    public PlanReader(String s) {
        filename = s;
    }

    public static void main(String[] args) {
        String path = System.getProperty("user.home") + System.getProperty("file.separator") + "newEvaluation/plan/storm"
                + System.getProperty("file.separator");
        for (int i = 1; i <= 100; i++) {
            PlanReader pr = new PlanReader(String.format("%splan%dfalse.json", path, i));
            pr.read();
        }


    }

    public void read() {
        ReconfigurationPlanConverter converter = new ReconfigurationPlanConverter();

        try {
            ReconfigurationPlan plan = converter.fromJSON(new File(filename));


            levelDependency(plan);

//            System.out.println(calculateAverage(plan));
//            System.out.println(plan);
        } catch (IOException e) {
            System.err.println();
        } catch (JSONConverterException e) {
            e.printStackTrace();
        }
    }

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

}
