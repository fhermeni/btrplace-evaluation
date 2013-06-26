package evaluation.generator;

import btrplace.json.AbstractJSONObjectConverter;
import btrplace.json.JSONArrayConverter;
import btrplace.json.JSONConverterException;
import btrplace.json.model.constraint.SatConstraintsConverter;
import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import evaluation.demo.Application;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: TU HUYNH DANG
 * Date: 6/19/13
 * Time: 4:48 PM
 */
public class ApplicationConverter extends AbstractJSONObjectConverter<Application> implements JSONArrayConverter<Application> {


    public ApplicationConverter(Model m) {
        setModel(m);
    }

    @Override
    public JSONObject toJSON(Application application) throws JSONConverterException {
        JSONObject object = new JSONObject();
        object.put("id", application.getId());
        object.put("tier1", vmsToJSON(application.getTier1()));
        object.put("tier2", vmsToJSON(application.getTier2()));
        object.put("tier3", vmsToJSON(application.getTier3()));
        object.put("constraints", constraintsToJSON(application.getConstraints()));
        return object;
    }

    private Object constraintsToJSON(Collection<SatConstraint> constraints) throws JSONConverterException {
        SatConstraintsConverter converter = new SatConstraintsConverter();
        return converter.toJSON(constraints);
    }

    @Override
    public Application fromJSON(JSONObject in) throws JSONConverterException {
        Application application = new Application();
        application.setId(requiredInt(in, "id"));
        application.setTier1(new ArrayList<>(requiredVMs(in, "tier1")));
        application.setTier2(new ArrayList<>(requiredVMs(in, "tier2")));
        application.setTier3(new ArrayList<>(requiredVMs(in, "tier3")));
        application.setConstraints(requiredConStraint(in, "constraints"));
        return application;
    }

    private Collection<SatConstraint> requiredConStraint(JSONObject o, String id) throws JSONConverterException {
        SatConstraintsConverter converter = new SatConstraintsConverter();
        converter.setModel(getModel());
        Object x = o.get(id);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("Set of ints expected at key '" + id + "'");
        }
        JSONArray x1 = (JSONArray) x;
        return converter.listFromJSON(x1);
    }

    @Override
    public List<Application> listFromJSON(JSONArray in) throws JSONConverterException {
        List<Application> l = new ArrayList<>(in.size());
        for (Object o : in) {
            if (!(o instanceof JSONObject)) {
                throw new JSONConverterException("Expected an array of JSONObject but got an array of " + o.getClass().getName());
            }
            l.add(fromJSON((JSONObject) o));
        }
        return l;
    }

    @Override
    public JSONArray toJSON(Collection<Application> e) throws JSONConverterException {
        JSONArray array = new JSONArray();
        for (Application app : e) {
            array.add(toJSON(app));
        }
        return array;
    }

    @Override
    public List<Application> listFromJSON(File path) throws IOException, JSONConverterException {
        try (BufferedReader in = new BufferedReader(new FileReader(path))) {
            return listFromJSON(in);
        }
    }

    @Override
    public List<Application> listFromJSON(String buf) throws IOException, JSONConverterException {
        try (StringReader in = new StringReader(buf)) {
            return listFromJSON(in);
        }
    }

    @Override
    public List<Application> listFromJSON(Reader r) throws IOException, JSONConverterException {
        try {
            JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
            Object o = p.parse(r);
            if (!(o instanceof JSONArray)) {
                throw new JSONConverterException("Unable to parse a JSONArray");
            }
            return listFromJSON((JSONArray) o);
        } catch (ParseException ex) {
            throw new JSONConverterException(ex);
        }
    }

    @Override
    public String toJSONString(Collection<Application> o) throws JSONConverterException {
        return toJSON(o).toJSONString();
    }

    @Override
    public void toJSON(Collection<Application> e, Appendable w) throws JSONConverterException, IOException {
        toJSON(e).writeJSONString(w);
    }

    @Override
    public void toJSON(Collection<Application> e, File path) throws JSONConverterException, IOException {
        try (FileWriter out = new FileWriter(path)) {
            toJSON(e, out);
        }
    }
}
