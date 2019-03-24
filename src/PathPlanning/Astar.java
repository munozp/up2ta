package PathPlanning;

import java.util.*;

/**
 * Implements the A* algorithm to search a path in a DEM.
 * @author Pablo Muñoz
 */
public class Astar extends SearchMethod {
    
    /** Open nodes list, sorted by F. */
    private List<Node> open;
    /** Closed nodes table. */
    private Hashtable closed;
    
    /**
     * Empty constructor.
     */
    public Astar()
    {
        super();
    }
    
    /**
     * Constructor for Astar class.
     * @param dem terrain info.
     * @param init start node.
     * @param end goal node.
     * @param heur heuristic used for search.
     * @param withz true for use Z values.
     * @param withc true for use transversal costs.
     */
    public Astar(Map dem, Node init, Node end, short heur, boolean withz, boolean withc)
    {
        super("A*", dem, init, end, heur, withz, withc, false);
        if(withz)
            this.heuristic = Heuristics.H_OCTILE_Z;
	open = new ArrayList();
        closed = new Hashtable();
    }
    
    @Override
    /**
     * Clear internal data of the algorithm.
     */
    protected void clear_internal_data()
    {
	open.clear();
	closed.clear();
    }
    
    /**
     * Search a path using the A* algorithm without vertex re-expansion.
     * @return True if a path is found or false if no path found.
     */
    @Override
    public boolean search()
    {
        if(!check_valid_data())
            return false;
        ArrayList<Node> succlist = new ArrayList();

        // Set initial state
	map.get_node(start.getX(), start.getY()).setParent(start);
        map.get_node(start.getX(), start.getY()).setF(0, get_h(start,start, goal));
        open.add(map.get_node(start.getX(), start.getY()));
        // Auxiliary variables
	Node pos, aux;

        // Begin search
        start_cpu_counter();
        while(!open.isEmpty())
        {            
            pos = open.remove(0);
            closed.put(pos.hashCode(), pos);
            expnodes++;

            // If position is solution => finish
            if(pos.equals(goal))
            {
                clear_internal_data();
                end_cpu_counter();
                return true;
            }
            
            // Generate succesors for actual point
            succlist = map.get_succesors_without_obstacles(pos);
            for(int i=0; i < succlist.size(); i++)
            {
                aux = succlist.get(i);
                if(closed.get(aux.hashCode()) == null)
                    update_vertex(pos, aux);
            }// End of succesor check
            // Sort open list
            Collections.sort(open);
            
        } // Open list is empty, no path found
        end_cpu_counter();
        return false;
    }

     /**
     * Update open list.
     * @param pos actual position.
     * @param succ successor to test.
     */
    private void update_vertex(Node pos, Node succ)
    {
        //double hsuc = get_h(succ, goal);
        float gsuc = get_g(pos, succ, true);
	if(gsuc < succ.getG() && gsuc > 0)
        {
	    int x = succ.getX();
	    int y = succ.getY();
	    map.get_node(x, y).setF(gsuc, get_h(pos, succ, goal));
	    map.get_node(x, y).setParent(pos);
            // Remove succesor from open if is contained and insert succesors in open list
            open.remove(map.get_node(x, y));
	    open.add(map.get_node(x, y));
        }
    }

    
    /**
     * Search a path using the A* algorithm with vertex re-expansion.
     * @return True if a path is found or false if no path found.
     */
    public boolean search_rex()
    {
        if(!check_valid_data())
            return false;
        open = new ArrayList();
        closed = new Hashtable();
        ArrayList<Node> succ = new ArrayList();
        
        // Set initial state
	    map.get_node(start.getX(), start.getY()).setF(0, get_h(start, start, goal));
        map.get_node(start.getX(), start.getY()).setParent(start);
        open.add(map.get_node(start.getX(), start.getY()));
        Node pos;
        // Auxiliary variables
        float gsuc, hsuc;
        int x, y;
        boolean insert;
        Node aux;
        
        // A* search
        start_cpu_counter();
        while(!open.isEmpty())
        {
            pos = open.remove(0);
            closed.put(pos.hashCode(), pos);
            expnodes++;
            
            // If position is solution => finish
            if(pos.equals(goal))
            {
                end_cpu_counter();
                return true;
            }
            
            // Generate succesors for actual point
            succ = map.get_succesors_without_obstacles(pos);        
            for(int i=0; i < succ.size(); i++)
            {
                insert = true;
                x = succ.get(i).getX();
                y = succ.get(i).getY();
                gsuc =  get_g(pos, succ.get(i), true);
                hsuc = get_h(pos, succ.get(i), goal);

                // Search in closed
                aux = (Node) closed.get(succ.get(i).hashCode());
                if(aux != null)
                {
                    // Delete node only if new node has less F value
                    if(aux.getF() > (gsuc + hsuc))
                        closed.remove(aux.hashCode());
                    else
                        insert = false;
                }
                // Search in open
                else if(open.contains(map.get_node(x, y)))
                {
                    int index = open.indexOf(map.get_node(x, y));
                    if(index >= 0)
                    {
                        aux = open.get(index);
                        // Delete node only if new node has less F value
                        if(aux.getF() > (gsuc + hsuc))
                            open.remove(aux);
                        else
                            insert = false;
                    }
                }
                // Add node to the open list if not exist or is better than open/closed
                if(insert)
                {
                    map.get_node(x, y).setParent(pos);
                    map.get_node(x, y).setF(gsuc, hsuc);
                    open.add(map.get_node(x, y));
                }
            }// End of succesor check
            // Sort open list
            Collections.sort(open);
        } // Open list is empty, no path found
        return false;
    }
    
    /**
     * Search a path using the A* algorithm without vertex re-expansion.
     * After search performs a post-smoothing process.
     * @return True if a path is found or false if no path found.
     */
    public boolean search_ps()
    {
            // Get current CPU time
            double cputime2 = get_cpu_time();
            // Restart CPU counter
            start_cpu_counter();
            
            // Post-smoothing fase
            int k = 0;
            ArrayList<Node> prepath = get_path();
            ArrayList<Node> newpath = new ArrayList<Node>();
            newpath.add(goal);
            for(int i = 1; i < prepath.size()-1; i++)
                if(!Geometry.LineOfSightB(map, newpath.get(k), prepath.get(i+1)))
                {
                    k++;
                    newpath.add(prepath.get(i));
                }
            newpath.add(start);
            
            // Set new cpu time
            end_cpu_counter();
            add_cpu_time(cputime2);
	        // Change algorithm name and mode
	        change_name("A* Post-smoothed");
	        change_mode(true);
            // Set new path
            set_new_path(newpath);
            return true;
    }
    
    /**
     * Print the open list.
     */
    private void print_open()
    {      
        System.out.println("Open:");
        for(int i=0; i < open.size(); i++)
            System.out.println("    " + open.get(i).toString() + "  P:"+open.get(i).getParent());
        System.out.println();
    }
    
    /**
     * Print the closed list (no sorted).
     */
    private void print_closed()
    {
        Enumeration<Node> li;
        li = closed.elements();
        System.out.println("Closed:");
        for(int i=0; i < closed.size(); i++)
            System.out.println("      "+li.nextElement().toString());
        System.out.println();
    }

}
