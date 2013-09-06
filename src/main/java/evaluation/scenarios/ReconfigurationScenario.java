package evaluation.scenarios;

import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.constraint.CMaxOnlines;
import btrplace.solver.choco.runner.SolvingStatistics;
import evaluation.demo.Application;
import evaluation.generator.ApplicationConverter;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.io.*;
import java.util.*;

/**
 * User: Tu Huynh Dang
 * Date: 6/16/13
 * Time: 2:52 PM
 */
public abstract class ReconfigurationScenario implements Runnable {

    static int TIME_OUT = 3600;
    protected static boolean findContinuous = false;
    int modelId;
    Model model;
    Set<SatConstraint> validateConstraint;
    Map<SatConstraint, Integer> checkMap;
    Collection<Application> applications;
    ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    StringBuilder sb;
    ShareableResource ecu;
    ShareableResource ram;
    String rp_type;
    String instance;
    String outPath;

    public ReconfigurationScenario(String instance, String out) throws ParseException, IOException, JSONConverterException {
        this.instance = instance;
        readData();
        outPath = out;
        sb = new StringBuilder();
        cra.setTimeLimit(TIME_OUT);
        cra.doRepair(true);
    }

    abstract boolean reconfigure(int p, boolean c);

    public static float[] currentLoad(Model measure_model) {
        float[] loads = new float[2];
        Mapping mapping = measure_model.getMapping();
        Set<Node> onlineNodes = mapping.getOnlineNodes();
        Set<VM> runningVMs = mapping.getRunningVMs();
        ShareableResource sr = (ShareableResource) measure_model.getView("ShareableResource.ecu");
        ShareableResource sr2 = (ShareableResource) measure_model.getView("ShareableResource.ram");
        float capacity = sr.sumCapacities(onlineNodes, true);
        float used = sr.sumConsumptions(runningVMs, true);
        loads[0] = used / capacity * 100;
        capacity = sr2.sumCapacities(onlineNodes, true);
        used = sr2.sumConsumptions(runningVMs, true);
        loads[1] = used / capacity * 100;
        return loads;
    }

    public static void setTimeOut(int timeout) {
        TIME_OUT = timeout;
    }

    public void readData() throws JSONConverterException, ParseException, IOException {
            JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
            JSONObject o = (JSONObject) p.parse(new BufferedReader(new FileReader(instance)));
            ModelConverter mc = new ModelConverter();

            model = mc.fromJSON((JSONObject) o.get("model"));
            ApplicationConverter ac = new ApplicationConverter(model);
            applications = ac.listFromJSON((JSONArray) o.get("slas"));
            ecu = (ShareableResource) model.getView(ShareableResource.VIEW_ID_BASE + "ecu");
            ram = (ShareableResource) model.getView(ShareableResource.VIEW_ID_BASE + "ram");
            cra.getConstraintMapper().register(new CMaxOnlines.Builder());
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
            validateConstraint = checkMap.keySet();
    }

    public void checkSatisfaction(ReconfigurationPlan plan, HashSet<Integer>[] constr,
                                  int[] DCconstraint, HashSet<Integer> apps) {
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
                    DCconstraint[0]++;
                } else if (s instanceof MaxOnline) {
                    DCconstraint[1]++;
                }
            }
            s.setContinuous(continuous);
        }
    }

    public static void findContinuous() {
        findContinuous = true;
    }

    public void viewConstraints(Collection<SatConstraint> constraints) {
        int spread = 0, among = 0, splita = 0, srec = 0, max = 0;

        for (SatConstraint constraint : constraints) {
            if (constraint instanceof Spread) spread++;
            if (constraint instanceof Among) among++;
            if (constraint instanceof SplitAmong) splita++;
            if (constraint instanceof SingleResourceCapacity) srec++;

            if (constraint instanceof MaxOnline) max++;
        }
        System.out.printf("%d\t%d\t%d\t%d\t%d\n", spread, among, splita, srec, max);
    }

    public void result(ReconfigurationPlan plan, HashSet<Integer>[] vc,
                       int[] dc, HashSet<Integer> app) throws IOException, JSONConverterException {

        ReconfigurationPlanConverter pc = new ReconfigurationPlanConverter();
        if (outPath != null) {
            pc.toJSON(plan, new BufferedWriter(new FileWriter(outPath)));
        }
        sb.append(String.format("%s\t", instance));
        sb.append(String.format("%d\t%d\t%d\t", vc[0].size(), vc[1].size(), vc[2].size()));
        sb.append(String.format("%d\t%d\t%d\t", dc[0], dc[1], app.size()));
        float[] load = currentLoad(model);
        sb.append(String.format("%f\t%f\t", load[0], load[1]));
        load = currentLoad(plan.getResult());
        sb.append(String.format("%f\t%f\t", load[0], load[1]));
        SolvingStatistics statistics = cra.getStatistics();
        sb.append(String.format("%d\t%d\t%d\n", statistics.getSolvingDuration(), plan.getDuration(), plan.getSize()));
    }

    @Override
    public String toString() {
        return sb.toString();
    }

}
