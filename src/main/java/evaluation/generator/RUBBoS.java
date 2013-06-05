package evaluation.generator;

import btrplace.model.Model;
import btrplace.model.VM;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Tu Huynh Dang
 * Date: 6/2/13
 * Time: 8:55 AM
 */
public class RUBBoS {
    private static Model model;
    private ArrayList<Set<VM>> tiers;
    private Set<VM> tier1;
    private Set<VM> tier2;
    private Set<VM> tier3;
    private Set<VM> tier4;

    public RUBBoS(int a, int b, int c, int d) {
        tier1 = new HashSet<VM>();
        tier2 = new HashSet<VM>();
        tier3 = new HashSet<VM>();
        tier4 = new HashSet<VM>();
        tiers = new ArrayList<Set<VM>>() {{
            add(tier1);
            add(tier2);
            add(tier3);
            add(tier4);
        }};

        for (int i = 0; i < a; i++) {
            VM vm = model.newVM();
            tier1.add(vm);
        }

        for (int i = 0; i < b; i++) {
            VM vm = model.newVM();
            tier2.add(vm);
        }

        for (int i = 0; i < c; i++) {
            VM vm = model.newVM();
            tier3.add(vm);
        }

        for (int i = 0; i < d; i++) {
            VM vm = model.newVM();
            tier4.add(vm);
        }

    }

    public static void setInfrastructure(Model model) {
        RUBBoS.model = model;
    }


    public Set<VM> getTier1() {
        return tier1;
    }

    public Set<VM> getTier2() {
        return tier2;
    }

    public Set<VM> getTier3() {
        return tier3;
    }

    public Set<VM> getTier4() {
        return tier4;
    }

    public ArrayList<Set<VM>> getTiers() {
        return tiers;
    }

    public static Model getModel() {
        return model;
    }

    public void print() {
        for (Set<VM> t : tiers) {
            System.out.println(t);
        }
    }
}