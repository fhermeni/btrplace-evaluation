package evaluation.demo;

import btrplace.model.Model;
import btrplace.model.VM;
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
    ArrayList<VM> tier1;
    ArrayList<VM> tier2;
    ArrayList<VM> tier3;
    ArrayList<ArrayList<VM>> tiers;

    public Application(Model model) {
        tier1 = new ArrayList<VM>();
        tier2 = new ArrayList<VM>();
        tier3 = new ArrayList<VM>();
        tiers = new ArrayList<ArrayList<VM>>();
        tiers.addAll(Arrays.asList(tier1, tier2, tier3));

        Random random = new Random(System.nanoTime() % 100000);
        for (int i = 0; i < 3; i++) {
            int n = random.nextInt(5) + 2;
            ShareableResource sr = (ShareableResource) model.getView("ShareableResource.cpu");

            switch (i) {
                case 0:
                    for (int j = 0; j < n; j++) {
                        VM vm = model.newVM(index++);
                        model.getMapping().addReadyVM(vm);
                        sr.setConsumption(vm, 1);
                        tiers.get(i).add(vm);
                    }
                    break;

                case 1:
                    for (int j = 0; j < n; j++) {
                        VM vm = model.newVM(index++);
                        sr.setConsumption(vm, 4);
                        model.getMapping().addReadyVM(vm);
                        tiers.get(i).add(vm);
                    }
                    break;

                default:
                    for (int j = 0; j < n; j++) {
                        VM vm = model.newVM(index++);
                        sr.setConsumption(vm, 2);
                        model.getMapping().addReadyVM(vm);
                        tiers.get(i).add(vm);
                    }
            }

        }
    }

    public Collection<SatConstraint> initConstraints() {
        Collection<SatConstraint> cts = new ArrayList<SatConstraint>();
        for (ArrayList<VM> t : tiers) {
            Spread spread = new Spread(new HashSet<VM>(t));
            Running run = new Running(t);
            cts.add(spread);
            cts.add(run);
        }
        return cts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Tier1: %s\nTier2: %s\nTier3: %s\n", tier1, tier2, tier3));
        return sb.toString();
    }
}
