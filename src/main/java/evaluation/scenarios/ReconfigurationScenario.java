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
import btrplace.solver.choco.constraint.CMaxOnline;
import btrplace.solver.choco.runner.SolvingStatistics;
import evaluation.demo.Application;
import evaluation.demo.PlanReader;
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

    protected int TIME_OUT = 3600;
    protected boolean findContinuous = false;
    Model model;
    Set<SatConstraint> validateConstraint;
    Map<SatConstraint, Integer> checkMap;
    Collection<Application> applications;
    ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    StringBuilder sb;
    ShareableResource ecu;
    ShareableResource ram;
    int rp_type;
    String instance;
    String outPath;

    public ReconfigurationScenario(String instance, String out) throws ParseException, IOException, JSONConverterException {
        this.instance = instance;
        readData();
        outPath = out;
        sb = new StringBuilder();
        cra.setTimeLimit(TIME_OUT);
        cra.doRepair(false);
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

    public void setTimeOut(int timeout) {
        TIME_OUT = timeout;
        cra.setTimeLimit(timeout);
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
            cra.getConstraintMapper().register(new CMaxOnline.Builder());
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

    public void findContinuous() {
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
            BufferedWriter b = new BufferedWriter(new FileWriter(outPath));
            pc.toJSON(plan, b);
            b.close();
        }
        sb.append(String.format("%s\t%d\t%d\t1\t", new File(instance).getName(), rp_type, findContinuous ? 1 : 0));
        //0: spread, 1: among, 2: splitAmong
        sb.append(String.format("%d\t%d\t%d\t", vc[0].size(), vc[1].size(), vc[2].size()));
        //0: singleResourceCapacity, 1: MaxOnline
        sb.append(String.format("%d\t%d\t%d\t", dc[0], dc[1], app.size()));
        float[] load = currentLoad(model);
        //0: ecu, 1: ram
        sb.append(String.format("%f\t%f\t", load[0], load[1]));
        load = currentLoad(plan.getResult());
        //0: ecu, 1: ram
        sb.append(String.format("%f\t%f\t", load[0], load[1]));
        SolvingStatistics st = cra.getStatistics();
        sb.append(String.format("%d\t%d\t%d\t%d\t%d\t", st.getCoreRPBuildDuration(), st.getSpeRPDuration(), st.getSolvingDuration(), plan.getDuration(), plan.getSize()));
        //
        int [] actions = PlanReader.countActions(plan);
        //0: BootVM, 1: MigrateVM, 2: Allocate, 3: BootNode, 4: ShutdownNode
        sb.append(String.format("%d\t%d\t%d\t%d\t%d\n", actions[0], actions[1], actions[2], actions[3], actions[4]));
    }

    public void reportIssue(boolean noSolutions) {
        sb.append(String.format("%s\t%d\t%d\t%d\t", new File(instance).getName(), rp_type, findContinuous ? 1 : 0, noSolutions ? 0 : -1));
        //Unable to state about the resulting SLAs violations
        sb.append("-\t-\t-\t");
        //The same for the DC constraints
        sb.append("-\t-\t-\t");
        float[] load = currentLoad(model);
        //0: ecu, 1: ram
        sb.append(String.format("%f\t%f\t", load[0], load[1]));
        //Unable to state about the future load
        sb.append("-\t-\t");
        SolvingStatistics st = cra.getStatistics();
        //No plan
        if (st != null) {
            sb.append(String.format("%d\t%d\t%d\t0\t0\t0\t0\t0\t0\t0\n", st.getCoreRPBuildDuration(), st.getSpeRPDuration(), st.getSolvingDuration()));
        } else {
            sb.append("0\t0\t0\t0\t0\t0\t0\t0\t0\t0\n");
        }
    }

    @Override
    public String toString() {
        return sb.toString();
    }

}
