package evaluation.demo;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.constraint.Gather;
import btrplace.model.constraint.Lonely;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.Spread;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Tu Huynh Dang
 * Date: 6/11/13
 * Time: 10:24 PM
 */
public class Application implements Serializable, Cloneable {
    private int index;
    private ArrayList<VM> tier1;
    private ArrayList<VM> tier2;
    private ArrayList<VM> tier3;
    private ArrayList<VM> vms;
    private ArrayList<ArrayList<VM>> tiers;
    private Collection<SatConstraint> validateConstraints;

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
        validateConstraints = new ArrayList<SatConstraint>();

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
        Collection<SatConstraint> spreads = new ArrayList<SatConstraint>(3);
        for (ArrayList<VM> t : tiers) {
            Spread spread = new Spread(new HashSet<VM>(t), cont);
            spreads.add(spread);
        }
        validateConstraints.addAll(spreads);
        return spreads;
    }

    public Collection<SatConstraint> gather(boolean cont) {
        Collection<SatConstraint> gathers = new ArrayList<SatConstraint>(2);
        for (int i = 0; i < 1; i++) {
            Collection<VM> gVM = new ArrayList<VM>();
            gVM.add(tier1.get(i));
            gVM.add(tier2.get(i));
            gVM.add(tier3.get(i));
            Gather gather = new Gather(gVM, cont);
            gathers.add(gather);
        }
        validateConstraints.addAll(gathers);
        return gathers;
    }

    public SatConstraint lonely(boolean cont) {
        return new Lonely(new HashSet<VM>(vms), cont);
    }


    public ArrayList<VM> getTier1() {
        return tier1;
    }

    public ArrayList<VM> getTier2() {
        return tier2;
    }

    public Collection<VM> getTier3() {
        return tier3;
    }

    public Collection<VM> getAllVM() {
        return vms;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Tier1: %s\nTier2: %s\nTier3: %s\n", tier1, tier2, tier3));
        return sb.toString();
    }
}
