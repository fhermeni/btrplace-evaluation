package evaluation.demo;

import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.model.*;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import evaluation.generator.ApplicationConverter;
import net.minidev.json.JSONObject;
import org.apache.commons.cli.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * User: Tu Huynh Dang
 * Date: 6/11/13
 * Time: 10:14 PM
 */
public class InstanceMaker implements Runnable {

    static int NUM_RACK = 16;
    static int NUM_APP = 350;
    static int NODES_PER_RACK = 16;
    static String path;
    Model model;
    boolean restriction;
    ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    ReconfigurationPlan plan = null;
    Collection<Collection<Node>> racks;
    Collection<Collection<Node>> groups;
    Collection<SatConstraint> validateConstraint;
    ArrayList<Application> appList;
    ShareableResource ecu;
    ShareableResource ram;
    private int modelId;

    public InstanceMaker(int id) {
        this(id, 16, 16, 350, System.getProperty("user.home"));
    }

    public InstanceMaker(int id, int r, int npr, int app, String out) {
        model = new DefaultModel();
        modelId = id;
        NODES_PER_RACK = npr;
        NUM_APP = app;
        NUM_RACK = r;
        path = out;
        restriction = false;
        racks = new ArrayList<>();
        groups = new ArrayList<>();
        validateConstraint = new ArrayList<>();
        appList = new ArrayList<>();
        cra.setTimeLimit(300);
        ecu = new ShareableResource("ecu", 64, 1);
        ram = new ShareableResource("ram", 128, 1);
        model.attach(ecu);
        model.attach(ram);
    }

    public static void main(String[] args) {
        //int id = 1;
        int racks = 16;
        int rpn = 16;
        int app = 350;
        //int nb = 100;
        String out = "instance.json";
        Options options = new Options();
        options.addOption("r", true, "number of racks");
        options.addOption("p", true, "number of nodes/packs");
        options.addOption("a", true, "number of applications");
        Option o = new Option("o", true, "output JSON file");
        o.setRequired(true);
        options.addOption(o);

        CommandLineParser parser = new BasicParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("r")) {
                racks = Integer.parseInt(line.getOptionValue("r"));
            }
            if (line.hasOption("p")) {
                rpn = Integer.parseInt(line.getOptionValue("p"));
            }
            if (line.hasOption("a")) {
                app = Integer.parseInt(line.getOptionValue("a"));
            }
            if (line.hasOption("o")) {
                out = line.getOptionValue("o");
            }
        }
        catch(ParseException e) {
            System.err.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(InstanceMaker.class.getSimpleName(), options);
            System.exit(1);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        InstanceMaker instanceMaker = new InstanceMaker(0, racks, rpn, app, out);
        instanceMaker.run();
    }

    public void run() {
        initMap();
        runMix();
        float[] load = currentLoad();
        System.out.printf("%f, %f\n", load[0], load[1]);
        try {
            storeModel2(path);
        } catch (Exception e) {
            System.err.println("Unable to generate instance '" + path  +"': " + e.getMessage());
        }
    }

    private void initMap() {
        Collection<Node> group1 = new ArrayList<>();
        Collection<Node> group2 = new ArrayList<>();
        for (int j = 0; j < NUM_RACK; j++) {
            ArrayList<Node> ns = new ArrayList<>();
            for (int i = 0; i < NODES_PER_RACK; i++) {
                Node node = model.newNode();
                ns.add(node);
                if (i == NODES_PER_RACK - 1)
                    model.getMapping().addOfflineNode(node);
                else model.getMapping().addOnlineNode(node);
            }
            racks.add(ns);
            if (j < 8) group1.addAll(ns);
            else group2.addAll(ns);
        }
        groups.add(group1);
        groups.add(group2);
    }

    private boolean runMix() {
        Collection<SatConstraint> constraints = new ArrayList<>();

        try {
            for (int i = 0; i < NUM_APP; i++) {
                Application app = new Application(model, i);
                runApplication(app, constraints);
                if (i % 4 == 0) makeHA(app, constraints);
            }
            SingleResourceCapacity SReC = new SingleResourceCapacity(model.getMapping().getAllNodes(), "ecu", 60, restriction);
            SingleResourceCapacity SReC2 = new SingleResourceCapacity(model.getMapping().getAllNodes(), "ram", 120, restriction);
            MaxOnline maxOnline = new MaxOnline(model.getMapping().getAllNodes(), 240, restriction);
            validateConstraint.add(maxOnline);
            validateConstraint.add(SReC);
            validateConstraint.add(SReC2);
            constraints.addAll(validateConstraint);
            plan = cra.solve(model, constraints);
            if (plan != null) {
                model = plan.getResult();
            }
        } catch (SolverException e) {
            e.printStackTrace();
            System.err.println("Run " + e.getMessage());
            return false;
        }
        return true;
    }

    private void runApplication(Application app, Collection<SatConstraint> constraints) {
        appList.add(app);
        for (VM vm : app.getTier2()) {
            ecu.setConsumption(vm, 4);
            ram.setConsumption(vm, 2);
        }

        for (VM vm : app.getTier3()) {
            ram.setConsumption(vm, 4);
        }
        Running run = new Running(app.getAllVM());
        constraints.add(run);
        Collection<SatConstraint> spread = app.spread(restriction);
        validateConstraint.addAll(spread);
        Among among = new Among(app.getTier3(), racks, restriction);
        app.addConstraint(among);
        validateConstraint.add(among);
    }

    private void makeHA(Application app, Collection<SatConstraint> constraints) {
        Collection<Collection<VM>> vms = new ArrayList<>();
        Application clone = new Application(app);
        runApplication(clone, constraints);

        vms.add(app.getAllVM());
        vms.add(clone.getAllVM());

        SplitAmong splitAmong = new SplitAmong(vms, groups);
        app.addConstraint(splitAmong);
        validateConstraint.add(splitAmong);
    }

    private float[] currentLoad() {
        float[] loads = new float[2];
        Mapping mapping = model.getMapping();
        Set<Node> onlineNodes = mapping.getOnlineNodes();
        Set<VM> runningVMs = mapping.getRunningVMs();
        ShareableResource sr = (ShareableResource) model.getView("ShareableResource.ecu");
        ShareableResource sr2 = (ShareableResource) model.getView("ShareableResource.ram");
        float capacity = sr.sumCapacities(onlineNodes, true);
        float used = sr.sumConsumptions(runningVMs, true);
        loads[0] = used / capacity * 100;
        capacity = sr2.sumCapacities(onlineNodes, true);
        used = sr2.sumConsumptions(runningVMs, true);
        loads[1] = used / capacity * 100;
        return loads;
    }

    private void storeModel2(String out) throws IOException, JSONConverterException {
        JSONObject o = new JSONObject();
        ModelConverter c = new ModelConverter();
        ApplicationConverter c2 = new ApplicationConverter(model);
        o.put("model", c.toJSON(model));
        o.put("slas", c2.toJSON(appList));
        BufferedWriter buf = new BufferedWriter(new FileWriter(out));
        o.writeJSONString(buf);
        buf.close();
    }
}
