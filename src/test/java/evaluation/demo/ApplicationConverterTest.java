package evaluation.demo;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import evaluation.generator.ApplicationConverter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: TU HUYNH DANG
 * Date: 6/19/13
 * Time: 5:00 PM
 */
public class ApplicationConverterTest {

    @Test
    public void testApplicationConverter() throws Exception {
        Model model = new DefaultModel();
        Application app = new Application(model, 1);
        app.spread(false);
        ApplicationConverter converter = new ApplicationConverter(model);
        File file = new File(System.getProperty("user.home") + "/Application.json");
        converter.toJSON(app, file);
        Application application = converter.fromJSON(file);
        Assert.assertEquals(app, application);
        System.out.println(application.getConstraints());
    }

    @Test
    public void testListApplicationConverter() throws Exception {
        Model model = new DefaultModel();
        Application app = new Application(model, 1);
        Application app2 = new Application(model, 2);
        List<Application> appList = new ArrayList<>();
        appList.add(app);
        appList.add(app2);
        ApplicationConverter converter = new ApplicationConverter(model);
        System.out.println(appList);

        File file = new File(System.getProperty("user.home") + "/Application2.json");
        converter.toJSON(appList, file);
        List<Application> application = converter.listFromJSON(file);
        Assert.assertEquals(appList, application);

    }
}
