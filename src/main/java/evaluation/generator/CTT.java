package evaluation.generator;

import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.view.ShareableResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: TU HUYNH DANG
 * Date: 5/31/13
 * Time: 2:19 PM
 */
public class CTT {
        public static final int TB = 1024;
    public static void main(String[] args) {

        Model model = new DefaultModel();
        List<Node> cbb = new ArrayList<Node>();
        ShareableResource cpu = new ShareableResource("cpu", 8, 1);
        ShareableResource ram = new ShareableResource("ram", 16, 1);
        ShareableResource storage = new ShareableResource("storage", 2 * TB, 1);
        model.attach(cpu);
        model.attach(ram);
        model.attach(storage);
        // Add compute building block nodes
        for (int i = 0; i < 128; i++) {
            Node node = model.newNode();
            cbb.add(node);
            model.getMapping().addOnlineNode(node);

        }
        // Add head nodes
        /*
        for (int i = 0; i < 2; i++) {
            Node node = model.newNode();
            cbb.add(node);
            ram.setCapacity(node, 8);
            storage.setCapacity(node, 292);
            model.getMapping().addOnlineNode(node);
        }
        // Add File Servers

        for (int i = 0; i < 4; i++) {
            Node node = model.newNode();
            cbb.add(node);
            storage.setCapacity(node, 72 * TB);
            model.getMapping().addOnlineNode(node);
        }
        */
        ModelConverter converter = new ModelConverter();
        try {
            converter.toJSON(model, new File("OpenCirrus.json"));
        } catch (JSONConverterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
