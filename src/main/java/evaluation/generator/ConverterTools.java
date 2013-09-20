package evaluation.generator;

import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.json.model.constraint.ConstraintsConverter;
import btrplace.json.model.constraint.MaxOnlineConverter;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.model.Model;
import btrplace.model.constraint.MaxOnline;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import evaluation.demo.Application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: TU HUYNH DANG
 * Date: 6/3/13
 * Time: 4:17 PM
 */
public class ConverterTools {

    public static void constraintsToFile(Collection<SatConstraint> constraints, String constraint_name) {
        ConstraintsConverter converter = ConstraintsConverter.newBundle();
        try {
            converter.toJSON(constraints, new File(constraint_name));

        } catch (JSONConverterException | IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static ArrayList<SatConstraint> getConstraints(Model model, String... constraint_files) {

        ConstraintsConverter satConstraintsConverter = ConstraintsConverter.newBundle();
        ArrayList<SatConstraint> ctrs = new ArrayList<>();
        try {
            for (String s : constraint_files) {
                satConstraintsConverter.setModel(model);
                List<SatConstraint> satConstraint = satConstraintsConverter.listFromJSON(new File(s));
                ctrs.addAll(satConstraint);
            }

        } catch (IOException | JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return ctrs;
    }

    public static void modelToFile(Model m, String s) {
        ModelConverter modelConverter = new ModelConverter();
        try {
            modelConverter.toJSON(m, new File(s));
        } catch (JSONConverterException | IOException e1) {
            e1.printStackTrace();
        }
    }

    public static Model getModelFromFile(String model_file) {
        ModelConverter modelConverter = new ModelConverter();
        try {
            return modelConverter.fromJSON(new File(model_file));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static void planToFile(ReconfigurationPlan plan, String output_file) {
        if (plan == null) {
            System.out.println("Plan is NULL");
            System.exit(0);
        }
        ReconfigurationPlanConverter rpc = new ReconfigurationPlanConverter();
        try {
            rpc.toJSON(plan, new File(output_file));
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static void applicationsToFile(Model model, Collection<Application> app, String output_file) {
        ApplicationConverter converter = new ApplicationConverter(model);
        try {
            converter.toJSON(app, new File(output_file));
        } catch (JSONConverterException | IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Application> getApplicationsFromFile(Model model, String app_file) {
        ApplicationConverter converter = new ApplicationConverter(model);
        List<Application> applications = null;
        try {
            applications = converter.listFromJSON(new File(app_file));
        } catch (IOException | JSONConverterException e) {
            e.printStackTrace();
        }
        return applications;
    }
}
