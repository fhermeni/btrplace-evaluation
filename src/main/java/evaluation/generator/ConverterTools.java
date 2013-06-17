package evaluation.generator;

import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.json.model.constraint.MaxOnlinesConverter;
import btrplace.json.model.constraint.SatConstraintsConverter;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 6/3/13
 * Time: 4:17 PM
 */
public class ConverterTools {

    public static void constraintsToFile(Collection<SatConstraint> constraints, String constraint_name) {
        SatConstraintsConverter converter = new SatConstraintsConverter();
        try {
            converter.register(new MaxOnlinesConverter());
            converter.toJSON(constraints, new File(constraint_name + "Constraint.json"));

        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static Set<SatConstraint> getConstraints(Model model, Collection<String> constraint_files) {

        SatConstraintsConverter satConstraintsConverter = new SatConstraintsConverter();
        satConstraintsConverter.register(new MaxOnlinesConverter());
        Set<SatConstraint> ctrs = new HashSet<SatConstraint>();
        try {
            for (String s : constraint_files) {
                satConstraintsConverter.setModel(model);
                List<SatConstraint> satConstraint = satConstraintsConverter.listFromJSON(new File(s));
                ctrs.addAll(satConstraint);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return ctrs;
    }

    public static void modelToFile(Model m, String s) {
        ModelConverter modelConverter = new ModelConverter();
        try {
            modelConverter.toJSON(m, new File(s));
        } catch (JSONConverterException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
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
}
