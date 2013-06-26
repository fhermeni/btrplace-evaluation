package evaluation.nouse;

import btrplace.json.JSONConverterException;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.plan.ReconfigurationPlan;

import java.io.File;
import java.io.IOException;

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
        String path = System.getProperty("user.home") + System.getProperty("file.separator") + "plan/sf"
                + System.getProperty("file.separator");
        PlanReader pr = new PlanReader(path + "4false");
        pr.read();


    }

    public void read() {
        ReconfigurationPlanConverter converter = new ReconfigurationPlanConverter();

        try {
            ReconfigurationPlan plan = converter.fromJSON(new File(filename));
            System.out.println(plan);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONConverterException e) {
            e.printStackTrace();
        }
    }

}
