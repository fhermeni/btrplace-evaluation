package evaluation.demo;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import org.testng.annotations.Test;

import java.io.File;

/**
 * User: TU HUYNH DANG
 * Date: 6/19/13
 * Time: 5:00 PM
 */
public class ApplicationConverterTest {
    @Test
    public void testFromJSON() throws Exception {
        ApplicationConverter converter = new ApplicationConverter();
        Application application = converter.fromJSON(new File("Application.json"));
        System.out.println(application);

    }

    @Test
    public void testToJSON() throws Exception {
        Model model = new DefaultModel();
        Application app = new Application(model);
        System.out.println(app);
        ApplicationConverter converter = new ApplicationConverter();
        converter.toJSON(app, new File(System.getProperty("user.home") + "/Application.json"));

    }
}
