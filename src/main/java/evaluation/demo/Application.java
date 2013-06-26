package evaluation.demo;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.constraint.Gather;
import btrplace.model.constraint.Lonely;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.Spread;

import java.io.Serializable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Tu Huynh Dang
 * Date: 6/11/13
 * Time: 10:24 PM
 */
public class Application implements Serializable, Cloneable {
    private int index;
    private int id;
    private ArrayList<VM> tier1;
    private ArrayList<VM> tier2;
    private ArrayList<VM> tier3;
    private ArrayList<VM> vms;
    private ArrayList<ArrayList<VM>> tiers;
    private Model model;
    private Collection<SatConstraint> constraints;

    public Application() {
        vms = new ArrayList<>();
        tier1 = new ArrayList<>();
        tier2 = new ArrayList<>();
        tier3 = new ArrayList<>();
        tiers = new ArrayList<>();
        constraints = new ArrayList<>();
    }

    public Application(Model m, int idx) {
        this();
        id = idx;
        model = m;
        index = model.getMapping().getAllVMs().size();
        vms = new ArrayList<>();
        tiers.add(tier1);
        tiers.add(tier2);
        tiers.add(tier3);

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

    public Application(Application origin) {
        this();
        id = origin.getId();
        model = origin.getModel();
        index = model.getMapping().getAllVMs().size();
        vms = new ArrayList<>();
        tiers.add(tier1);
        tiers.add(tier2);
        tiers.add(tier3);

        for (int i = 0; i < 3; i++) {
            switch (i) {
                case 0:
                    for (int j = 0; j < origin.getTier1().size(); j++) {
                        VM vm = model.newVM(index++);
                        model.getMapping().addReadyVM(vm);
                        tiers.get(i).add(vm);
                        vms.add(vm);
                    }
                    break;

                case 1:
                    for (int j = 0; j < origin.getTier2().size(); j++) {
                        VM vm = model.newVM(index++);
                        model.getMapping().addReadyVM(vm);
                        tiers.get(i).add(vm);
                        vms.add(vm);
                    }
                    break;

                default:
                    for (int j = 0; j < origin.getTier3().size(); j++) {
                        VM vm = model.newVM(index++);
                        model.getMapping().addReadyVM(vm);
                        tiers.get(i).add(vm);
                        vms.add(vm);
                    }
            }

        }
    }

    public Collection<SatConstraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(Collection<SatConstraint> constraints) {
        this.constraints = constraints;
    }

    public Collection<SatConstraint> spread(boolean cont) {
        Collection<SatConstraint> spreads = new ArrayList<>(3);
        for (ArrayList<VM> t : tiers) {
            Spread spread = new Spread(new HashSet<>(t), cont);
            spreads.add(spread);
        }
        constraints.addAll(spreads);
        return spreads;
    }

    public void addConstraint(SatConstraint... constraints) {
        Collections.addAll(this.constraints, constraints);
    }

    public Collection<SatConstraint> gather(boolean cont) {
        Collection<SatConstraint> gathers = new ArrayList<>(2);
        for (int i = 0; i < 1; i++) {
            Collection<VM> gVM = new ArrayList<>();
            gVM.add(tier1.get(i));
            gVM.add(tier2.get(i));
            gVM.add(tier3.get(i));
            Gather gather = new Gather(gVM, cont);
            gathers.add(gather);
        }
        return gathers;
    }

    public SatConstraint lonely(boolean cont) {
        return new Lonely(new HashSet<>(vms), cont);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTier1(ArrayList<VM> tier1) {
        this.tier1 = tier1;
    }

    public void setTier2(ArrayList<VM> tier2) {
        this.tier2 = tier2;
    }

    public void setTier3(ArrayList<VM> tier3) {
        this.tier3 = tier3;
    }

    public void setTiers(ArrayList<ArrayList<VM>> tiers) {
        this.tiers = tiers;
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

    public Model getModel() {
        return model;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Tier1: %s\nTier2: %s\nTier3: %s\n", tier1, tier2, tier3));
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Application)) {
            return false;
        }
        Application a2 = (Application) other;
        if (id != a2.getId())
            return false;
        else if (!tier1.containsAll(a2.getTier1())) {
            return false;
        } else if (!tier2.containsAll(a2.getTier2())) {
            return false;
        } else if (!tier3.containsAll(a2.getTier3())) {
            return false;
        }
        return true;
    }


}
