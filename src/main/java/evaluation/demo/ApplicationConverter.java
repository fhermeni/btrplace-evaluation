package evaluation.demo;

import btrplace.json.AbstractJSONObjectConverter;
import btrplace.json.JSONConverterException;
import btrplace.model.VM;
import net.minidev.json.JSONObject;

import java.util.ArrayList;

/**
 * User: TU HUYNH DANG
 * Date: 6/19/13
 * Time: 4:48 PM
 */
public class ApplicationConverter extends AbstractJSONObjectConverter<Application> {
    @Override
    public Application fromJSON(JSONObject in) throws JSONConverterException {
        Application application = new Application();
        application.setTier1((ArrayList<VM>) in.get("tier1"));
        application.setTier2((ArrayList<VM>) in.get("tier2"));
        application.setTier3((ArrayList<VM>) in.get("tier3"));
        return application;
    }

    @Override
    public JSONObject toJSON(Application application) throws JSONConverterException {
        JSONObject object = new JSONObject();
        object.put("tier1", application.getTier1());
        object.put("tier2", application.getTier2());
        object.put("tier3", application.getTier3());
        return object;
    }
}
