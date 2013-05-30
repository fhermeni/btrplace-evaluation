package evaluation.generator;

import btrplace.model.DefaultMapping;
import btrplace.model.Mapping;
import btrplace.model.view.ShareableResource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 5/2/13
 * Time: 9:16 AM
 */
public class TraceReader {

    private String model_file;
    private String assignment_file;
    private int number_resources, number_of_nodes, number_of_services, number_of_vm, number_of_balance_cost;
    private Map<Integer, Set<UUID>> neighbors;
    private Map<Integer, Set<UUID>> locations;
    private ArrayList<Integer> spreadMin;
    private ArrayList<Integer> process_move_costs;
    private Map<Integer, ArrayList<Integer>> map_service_depend;
    private Map<Integer, ArrayList<Integer>> balance_triple;
    private ArrayList<Integer> costs;
    private Map<Integer, ArrayList<Integer>> resource_properties;

    // For BtrPlace
    private ArrayList<UUID> nodes;
    private ArrayList<UUID> vms;
    private ArrayList<ShareableResource> shareableResources;
    private Map<Integer, Set<UUID>> allServices;
    private Map<Integer, Integer> assignment;
    private Mapping mapping;
    private int services_non_unit;
    private int max_per_service;

    public TraceReader(String model, String assignmentPath) throws IOException {
        model_file = model;
        assignment_file = assignmentPath;
        number_resources = 0;
        number_of_nodes = 0;
        number_of_services = 0;
        number_of_vm = 0;
        number_of_balance_cost = 0;
        shareableResources = new ArrayList<ShareableResource>();
        resource_properties = new HashMap<Integer, ArrayList<Integer>>();
        nodes = new ArrayList<UUID>();
        vms = new ArrayList<UUID>();
        neighbors = new HashMap<Integer, Set<UUID>>();
        locations = new HashMap<Integer, Set<UUID>>();
        spreadMin = new ArrayList<Integer>();
        process_move_costs = new ArrayList<Integer>();
        map_service_depend = new HashMap<Integer, ArrayList<Integer>>();
        allServices = new HashMap<Integer, Set<UUID>>();
        balance_triple = new HashMap<Integer, ArrayList<Integer>>();
        costs = new ArrayList<Integer>(4);
        assignment = new HashMap<Integer, Integer>();
        mapping = new DefaultMapping();
        services_non_unit = 0;
        max_per_service = 0;
    }

    public String getAssignment_file() {
        return assignment_file;
    }

    public Map<Integer, ArrayList<Integer>> getResource_properties() {
        return resource_properties;
    }

    public Set<UUID> getVms() {
        return new HashSet<UUID>(vms);
    }

    public Map<Integer, Integer> getAssignment() {
        return assignment;
    }

    public Mapping getMapping() {
        return mapping;
    }

    public void readModel() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(model_file));
        String line;

        //--------------- resources ----------
        if ((line = br.readLine()) != null) {
            number_resources = Integer.parseInt(line);
            for (int i = 0; i < number_resources; i++) {
                StringTokenizer st = new StringTokenizer(br.readLine());
                ArrayList<Integer> rp = new ArrayList<Integer>();
                while (st.hasMoreTokens()) {
                    rp.add(Integer.parseInt(st.nextToken()));
                }
                resource_properties.put(i, rp);
                shareableResources.add(new ShareableResource("r" + i, 1));
            }
        }

        //--------------- machines ----------
        if ((line = br.readLine()) != null) {
            number_of_nodes = Integer.parseInt(line);
            for (int i = 0; i < number_of_nodes; i++) {
                UUID nodej = new UUID(1, i);
                mapping.addOnlineNode(nodej);
                nodes.add(i, nodej);
                StringTokenizer st = new StringTokenizer(br.readLine());

                if (st.hasMoreTokens()) {
                    int nid = Integer.parseInt(st.nextToken());
                    if (neighbors.containsKey(nid)) {
                        neighbors.get(nid).add(nodej);
                    } else {
                        Set<UUID> uuids = new HashSet<UUID>();
                        uuids.add(nodej);
                        neighbors.put(nid, uuids);
                    }
                }

                if (st.hasMoreTokens()) {
                    int lid = Integer.parseInt(st.nextToken());
                    if (locations.containsKey(lid)) {
                        locations.get(lid).add(nodej);
                    } else {
                        Set<UUID> uuids = new HashSet<UUID>();
                        uuids.add(nodej);
                        locations.put(lid, uuids);
                    }
                }

                for (int j = 0; j < number_resources; j++) {
                    if (st.hasMoreTokens()) {
                        int resource_capacity = Integer.parseInt(st.nextToken());
                        shareableResources.get(j).set(nodej, resource_capacity);
                    }
                }
            }
        }
        //--------------- services ----------
        if ((line = br.readLine()) != null) {
            number_of_services = Integer.parseInt(line);
        }
        for (int s = 0; s < number_of_services; s++) {
            StringTokenizer service = new StringTokenizer(br.readLine());
            spreadMin.add(Integer.parseInt(service.nextToken()));

            int dependOn = Integer.parseInt(service.nextToken());
            max_per_service += dependOn;
            if (dependOn > 0) {
                services_non_unit++;
                ArrayList<Integer> depend_list = new ArrayList<Integer>();
                while (service.hasMoreTokens()) {
                    depend_list.add(Integer.parseInt(service.nextToken()));
                }
                map_service_depend.put(s, depend_list);
            }

        }

        //--------------- processes -----------
        if ((line = br.readLine()) != null) {
            number_of_vm = Integer.parseInt(line);
        }

        for (int k = 0; k < number_of_vm; k++) {
            UUID vm = new UUID(0, k);
            vms.add(vm);
            if ((line = br.readLine()) != null) {
                StringTokenizer vm_spec = new StringTokenizer(line);

                int service_id = Integer.parseInt(vm_spec.nextToken());
                if (allServices.containsKey(service_id)) {
                    Set<UUID> contain_vms = allServices.get(service_id);
                    contain_vms.add(vm);
                } else {
                    Set<UUID> contain_vms = new HashSet<UUID>();
                    contain_vms.add(vm);
                    allServices.put(service_id, contain_vms);
                }

                for (int j = 0; j < number_resources; j++) {
                    if (vm_spec.hasMoreTokens()) {
                        int resource_demand = Integer.parseInt(vm_spec.nextToken());
                        shareableResources.get(j).set(vm, resource_demand);
                    }
                }

                process_move_costs.add(Integer.parseInt(vm_spec.nextToken()));
            }
        }

        //--------------- balance triples -----------
        if ((line = br.readLine()) != null) {
            number_of_balance_cost = Integer.parseInt(line);
            if (number_of_balance_cost > 0) {
                for (int y = 0; y < number_of_balance_cost; y++) {
                    StringTokenizer bst = new StringTokenizer(br.readLine());
                    ArrayList<Integer> triple = new ArrayList<Integer>();
                    while (bst.hasMoreTokens()) {
                        triple.add(Integer.parseInt(bst.nextToken()));
                    }
                    balance_triple.put(y, triple);
                }
            } else {
                costs.add(0);
            }
        }

        //--------------- costs -----------
        while ((line = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line);
            while (st.hasMoreTokens())
                costs.add(Integer.parseInt(st.nextToken()));
        }

        br.close();
    }

    public void readAssignment() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(assignment_file));
        String line;
        while ((line = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line);
            int i = 0;
            while (st.hasMoreTokens()) {
                int host = Integer.parseInt(st.nextToken());
                assignment.put(i, host);
                mapping.addRunningVM(vms.get(i), nodes.get(host));


                i++;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Number of:\nNodes: %d\nResources: %d\nVMs: %d\nServices: %d\n" +
                "Neighborhoods: %d\nLocation: %d\nDependences: %d\nBalance costs: %d\n",
                number_of_nodes, number_resources, number_of_vm, number_of_services, neighbors.size(),
                locations.size(), services_non_unit, number_of_balance_cost));

        sb.append("nodes:\n");
        for (UUID n : nodes) {
            sb.append(n + "\n");
        }

        sb.append("neighborhoods:\n");
        for (Integer key : neighbors.keySet()) {
            sb.append(key + ":" + neighbors.get(key) + "\n");
        }
        sb.append("locations:\n");
        for (Integer key : locations.keySet()) {
            sb.append(key + ":" + locations.get(key) + "\n");
        }

        sb.append("Sharable Resources:\n");
        for (ShareableResource sr : shareableResources) {
            sb.append(sr + "\n");
        }

        sb.append("SpreadMin: " + spreadMin + "\n");
        sb.append("PMC: " + process_move_costs + "\n");

        sb.append("Dependencies:\n");
        for (Integer key : map_service_depend.keySet()) {
            sb.append(key + " depends on: " + map_service_depend.get(key) + "\n");
        }

        sb.append("Services:\n");
        for (Integer key : allServices.keySet()) {
            sb.append(key + " contains: " + allServices.get(key) + "\n");
        }

        for (Integer key : balance_triple.keySet()) {
            sb.append("balance tripe: " + balance_triple.get(key) + "\n");
        }

        sb.append("Assignment:\n");

        for (Integer key : assignment.keySet()) {
            sb.append(key + "=" + assignment.get(key) + "\n");
        }

        return sb.toString();
    }

    public int getNumber_resources() {
        return number_resources;
    }

    public int getNumber_of_nodes() {
        return number_of_nodes;
    }

    public int getNumber_of_services() {
        return number_of_services;
    }

    public int getNumber_of_vm() {
        return number_of_vm;
    }

    public int getNumber_of_balance_cost() {
        return number_of_balance_cost;
    }

    public ArrayList<ShareableResource> getShareableResources() {
        return shareableResources;
    }

    /**
     * This method returns the summary of the data set
     *
     * @return String shortly describes the data set
     */
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Number of:\nNodes: %d\nResources: %d\nVMs: %d\nServices: %d\n" +
                "Neighborhoods: %d\nLocation: %d\nDependencies: %d\nBalance costs: %d\n",
                number_of_nodes, number_resources, number_of_vm, getServicesSpread().size(), neighbors.size(),
                locations.size(), max_per_service, number_of_balance_cost));
        return sb.toString();
    }

    /**
     * This method returns all the nodes in the data set
     *
     * @return Set of all nodes
     */
    public Set<UUID> getNodes() {
        return new HashSet<UUID>(nodes);
    }

    /**
     * This method returns a map indicates each neighborhood with the nodes belonging to it
     *
     * @return Map of neighbors
     */
    public Map<Integer, Set<UUID>> getNeighborMap() {
        return neighbors;
    }

    /**
     * This method returns a map that indicates each location with the nodes belonging to it
     *
     * @return Map of location
     */
    public Map<Integer, Set<UUID>> getLocationMap() {
        return locations;
    }

    /**
     * This method returns an array of minimum numbers of spread requirement for each services
     *
     * @return Array of Integer
     */
    public ArrayList<Integer> getSpreadMin() {
        return spreadMin;
    }

    /**
     * Get the required number of spread of a specific service
     *
     * @param index
     * @return Integer indicates minimum of spread locations of a service
     */
    public int getSpreadMin(int index) {
        return spreadMin.get(index);
    }

    /**
     * This method returns the Process Move Cost of all processes
     *
     * @return
     */
    public ArrayList<Integer> getProcess_move_costs() {
        return process_move_costs;
    }

    /**
     * This method returns the Process Move Cost of a specific process
     *
     * @param processId
     * @return Process Move Cost
     */
    public int getProcess_move_costs(int processId) {
        return process_move_costs.get(processId);
    }


    /**
     * This method returns the dependency map which indicates which services depends on other services.
     *
     * @return Map of dependency. Key is processId and value is the list of processes that it depends on.
     */
    public Map<Integer, ArrayList<Integer>> getDependencyMap() {
        return map_service_depend;
    }

    /**
     * This method returns all services
     *
     * @return
     */
    public Map<Integer, Set<UUID>> getAllServices() {
        return allServices;
    }


    /**
     * This method returns the balance triples
     *
     * @return Map (TripleId, BalanceTriple)
     */
    public Map<Integer, ArrayList<Integer>> getBalanceTriple() {
        return balance_triple;
    }


    /**
     * This method returns all weights of the costs:
     * 1. weight of Balance Cost
     * 2. weight of Process Move Cost
     * 3. weight of Service Move Cost
     * 4. weight of Machine Move Cost
     *
     * @return An Array of Weights of the Costs
     */
    public ArrayList<Integer> getCosts() {
        return costs;
    }

    /**
     * This method returns the services having more than one process
     *
     * @return Map(service, [processes]);
     */
    public Map<Integer, Set<UUID>> getServicesSpread() {
        Map<Integer, Set<UUID>> map = new HashMap<Integer, Set<UUID>>();
        for (Integer key : allServices.keySet()) {
            if (allServices.get(key).size() > 1) {
                map.put(key, allServices.get(key));
            }
        }
        return map;
    }


    /**
     * This method returns the services having only one process
     *
     * @return Map(service, [processes]);
     */
    public Map<Integer, Set<UUID>> getServicesSingle() {
        Map<Integer, Set<UUID>> map = new HashMap<Integer, Set<UUID>>();
        for (Integer key : allServices.keySet()) {
            if (allServices.get(key).size() == 1) {
                map.put(key, allServices.get(key));
            }
        }
        return map;
    }
}
