package evaluation.generator;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.view.ShareableResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * User: Tu Huynh Dang
 * Date: 6/2/13
 * Time: 3:02 PM
 */
public class TestBtrPlace {

    @Test
    public void sumCapacity() {
        Model m = new DefaultModel();
        ShareableResource cpu = new ShareableResource("cpu", 4, 1);
        Node n = m.newNode();
        Node n2 = m.newNode();
        m.attach(cpu);
        Assert.assertEquals(cpu.sumCapacities(Arrays.asList(n, n2), true), 8);
    }
}
