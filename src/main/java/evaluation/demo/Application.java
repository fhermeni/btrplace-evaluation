package evaluation.demo;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.constraint.Preserve;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.Spread;
import btrplace.model.view.ModelView;
import btrplace.model.view.ShareableResource;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Tu Huynh Dang
 * Date: 6/11/13
 * Time: 10:24 PM
 */
public class Application {
     private static int index = 0;
    private ArrayList<VM> tier1;
    private ArrayList<VM> tier2;
    private ArrayList<VM> tier3;
    private ArrayList<VM> vms;
    private ArrayList<ArrayList<VM>> tiers;
    private Collection<SatConstraint> checkConstraints;

    public Application(Model model) {
        vms = new ArrayList<VM>();
        tier1 = new ArrayList<VM>();
        tier2 = new ArrayList<VM>();
        tier3 = new ArrayList<VM>();
        tiers = new ArrayList<ArrayList<VM>>();
        boolean b = tiers.addAll(Arrays.asList(tier1, tier2, tier3));
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
                    }
                    break;

                default:
                    for (int j = 0; j < n; j++) {
                        VM vm = model.newVM(index++);
                        model.getMapping().addReadyVM(vm);
                        tiers.get(i).add(vm);
                    }
            }

        }
    }

    public Collection<SatConstraint> initConstraints(boolean cont) {
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
//        constraints.add(new Preserve(tier2, "cpu", 4));
        int size = tier3.size() * percent / 100;
        ArrayList<VM> tmp = new ArrayList<VM>();
        for (int i = 0; i < size; i++) {
            tmp.add(tier3.get(i));
        }
        constraints.add(new Preserve(tmp, "cpu", 4));
        return constraints;
    }

    public Collection<VM> getAllVM() {
        return vms;
    }

    public Collection<VM> getDatabaseVM() {
        return tier3;
    }
}
