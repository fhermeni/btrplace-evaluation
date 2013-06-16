package evaluation.demo;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.constraint.*;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Tu Huynh Dang
 * Date: 6/11/13
 * Time: 10:24 PM
 */
public class Application {
    private int index;
    private ArrayList<VM> tier1;
    private ArrayList<VM> tier2;
    private ArrayList<VM> tier3;
    private ArrayList<VM> vms;
    private ArrayList<ArrayList<VM>> tiers;
    private Collection<SatConstraint> checkConstraints;

    public Application(Model model) {
        index = model.getMapping().getAllVMs().size();
        vms = new ArrayList<VM>();
        tier1 = new ArrayList<VM>();
        tier2 = new ArrayList<VM>();
        tier3 = new ArrayList<VM>();
        tiers = new ArrayList<ArrayList<VM>>();
        tiers.add(tier1);
        tiers.add(tier2);
        tiers.add(tier3);
        checkConstraints = new ArrayList<SatConstraint>();

        Random random = new Random(System.nanoTime() % 100000);
        for (int i = 0; i < 3; i++) {
            int n = random.nextInt(5) + 2;
            switch (i) {
                case 0:
                    for (int j = 0; j < n; j++) {
                        VM vm = model.newVM(index++);
                        model.getMapping().addReadyVM(vm);
                        tiers.get(i).add(vm);
                        vms.add(vm);
                    }
                    break;

                case 1:
                    for (int j = 0; j < n; j++) {
                        VM vm = model.newVM(index++);
                        model.getMapping().addReadyVM(vm);
                        tiers.get(i).add(vm);
                        vms.add(vm);
                    }
                    break;

                default:
                    for (int j = 0; j < n; j++) {
                        VM vm = model.newVM(index++);
                        model.getMapping().addReadyVM(vm);
                        tiers.get(i).add(vm);
                        vms.add(vm);
                    }
            }

        }
    }

    public Collection<SatConstraint> spread(boolean cont) {
        Collection<SatConstraint> cts = new ArrayList<SatConstraint>();
        for (ArrayList<VM> t : tiers) {
            Spread spread = new Spread(new HashSet<VM>(t), cont);
            Running run = new Running(t);
            cts.add(spread);
            checkConstraints.add(spread);
            cts.add(run);
        }
        return cts;
    }

    public Collection<SatConstraint> gather(boolean cont) {
        Collection<SatConstraint> gathers = new ArrayList<SatConstraint>();
        for (ArrayList<VM> t : tiers) {
            Running run = new Running(t);
            gathers.add(run);
        }
        for (int i = 0; i < 1; i++) {
            Collection<VM> gVM = new ArrayList<VM>();
            gVM.add(tier1.get(i));
            gVM.add(tier2.get(i));
            gVM.add(tier3.get(i));
            Gather gather = new Gather(gVM, cont);
            checkConstraints.add(gather);
            gathers.add(gather);
        }
        return gathers;
    }

    public Collection<SatConstraint> lonely(boolean cont) {
        Collection<SatConstraint> constraints = new ArrayList<SatConstraint>();
        Running run = new Running(vms);
        constraints.add(run);
        Lonely lonely = new Lonely(new HashSet<VM>(vms), cont);
        checkConstraints.add(lonely);
        return constraints;
    }
    public void setCheckConstraints(SatConstraint checkConstraints) {
        this.checkConstraints = Collections.singleton(checkConstraints);
    }

    public void setCheckConstraints(Collection<SatConstraint> checkConstraints) {
        this.checkConstraints = checkConstraints;
    }

    public Collection<SatConstraint> getCheckConstraints() {
        return checkConstraints;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Tier1: %s\nTier2: %s\nTier3: %s\n", tier1, tier2, tier3));
        return sb.toString();
    }

    public Collection<SatConstraint> loadSpike(int percent) {
        Collection<SatConstraint> constraints = new ArrayList<SatConstraint>();
        int size = tier2.size() * percent / 100;
        ArrayList<VM> tmp = new ArrayList<VM>();
        for (int i = 0; i < size; i++) {
            tmp.add(tier2.get(i));
        }
        constraints.add(new Preserve(tmp, "cpu", 16));
//        constraints.add(new Preserve(tmp, "ram", 17));
        return constraints;
    }

    public Collection<VM> getAllVM() {
        return vms;
    }

    public Collection<VM> getDatabaseVM() {
        return tier3;
    }
}
