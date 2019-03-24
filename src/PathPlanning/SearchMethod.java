package PathPlanning;

import java.util.ArrayList;

/**
 * Base class for a search algorithm.
 * @author Pablo Muñoz
 */
public abstract class SearchMethod extends Thread {

    /** DEM and traversal costs grid. */
    protected Map map;
    /** Heuristics used. */
    protected short heuristic;
    /** Specify if elevation values are used during search. */
    protected boolean withz;
    /** Specify if traversal costs values are used during search. */
    protected boolean withc;
    /** Specify if the algorithm is any-angle. */
    protected boolean anyangle;
    /** Number of expanded nodes. */
    protected int expnodes;
    /** Number of re-expanded nodes. */
    protected int reexpnodes;
    /** Flag to indicate if the search algorithm uses node re-expansion. */
    protected boolean reexpansion;
    /** Number of heading changes. */
    private int hchanges;
    /** Average degree heading change. */
    private double beta;
    /** Amount of turned degrees during path. */
    private double degrees;
    /** Path length. */
    private double pathlength;
    /** CPU time spent in search (nanosec). */
    private double cputime;
    /** Start node. */
    protected Node start;
    /** Destination node. */
    protected Node goal;
    /** Last path found by the algorithm. null if there is no path. */
    private ArrayList<Node> path;
    /** Name of the search algorithm implemented. */
    private String name;
    /** Information about JVM to get CPU time of the thread.
    @deprecated */
    private java.lang.management.ThreadMXBean thread;
    /** Euclidean distance from start node to goal node. */
    protected float dsg;
    /** Delta factor for alpha penalization. */
    protected static float CFACTOR = 0;
    /** Indicates if the class was previously instantiated. */
    private boolean instantiated = false;

    /**
     * Empty constructor
     */
    public SearchMethod()
    {}
    
    /**
     * Full constructor for search method.
     * @param nam name of the algorithm.
     * @param dem terrain info.
     * @param init start node.
     * @param end goal node.
     * @param h heuristic used for search.
     * @param z true for use Z values.
     * @param c true for use transversal costs.
     * @param a true for any-angle algorithm. False otherwise.
     */
    public SearchMethod(String nam, Map dem, Node init, Node end, short h, boolean z, boolean c, boolean a)
    {
        name = nam;
	anyangle = a;
        map = dem;
        heuristic = h;
        withz = z;
        withc = c;
	restart_search(init, end);
    }
    
    /** 
     * Initialize the search process with new start and goal nodes.
     * @note DEM information does not change.
     */
    public final void restart_search(Node init, Node end)
    {
        expnodes = 0;
        reexpnodes = 0;
        reexpansion = false;
        hchanges = 0;
        beta = 0;
        degrees = Double.MAX_VALUE;
        pathlength = -1;
	path = null;
	start = null;
	goal = null;
        if(instantiated)
	{
	    map.restart_map(true);
	    clear_internal_data();
	}
	else
	    instantiated = true;      
        if(set_start(init))
            start.setF(0,0);       
        set_goal(end);
	if(start != null && goal != null)
	    dsg = Heuristics.Heuclidean(start, goal);
    }
    
    /**
     * Allows to change the name of the search algorithm.
     * @param newname new algorithm name.
     */
    public void change_name(String newname)
    {
        name = newname;
    }

    /**
     * Allows to change any-angle mode of the algorithm.
     * @param newmode true for any-angle algorithm. False otherwise.
     */
    protected void change_mode(boolean newmode)
    {
	anyangle = newmode;
    }
    
    /**
     * Clear internal data of the search algorithm (open list, parent's nodes...) 
     * in order to restart the search algorithm. Called after restart_search. 
     */
    protected abstract void clear_internal_data();
    
    /**
     * Search a path using the map information and start and goal points.
     * @return True if a path is found or false if no path found.
     */
    public abstract boolean search();
    @Override
    public void run()
    {
        search();
    }

    /**
     * Check if all information needed to search is valid.
     * @return true if the search can be executed.
     */
    protected boolean check_valid_data()
    {
        return (start != null && goal != null && (map.valid_dem() || map.valid_cost_map()));
    }
    
    /**
     * Set a new start node.
     * @param init new start position.
     * @return true if the position is valid, false otherwise.
     */
    public final boolean set_start(Node init)
    {
        if(init != null && map.valid_pos(init.getX(), init.getY()) && 
                (map.get_tcost(init) <= Map.MAX_COST || map.get_isCornernode())&& 
                !init.isObstacle() && map.get_succesors_without_obstacles(init).size() > 0)
        {
            start = map.get_node(init.getX(), init.getY());
            return true;
        }
        else
            return false;
    }
    
    /**
     * Set a new goal node.
     * @param end new destination position.
     * @return true if the position is valid, false otherwise.
     */
    public final boolean set_goal(Node end)
    {
        if(end != null && map.valid_pos(end.getX(), end.getY()) && 
                (map.get_tcost(end) <= Map.MAX_COST || map.get_isCornernode()) && 
                !end.isObstacle() && map.get_succesors_without_obstacles(end).size() > 0)
        {
            goal = map.get_node(end.getX(), end.getY());
            return true;
        }
        else
	    return false;
    }

    /**
     * Change de delta factor.
     * @param delta new delta value (positive values only).
     */
    public static void changeCfactor(Float delta)
    {
        CFACTOR = delta;
    }
    
    /**
     * Calculates a particular heuristics for two nodes.
     * @param pos start node.
     * @param dest destination node.
     * @return the heuristic value.
     */
    protected float get_h(Node pos, Node succ, Node dest)
    {
        float h;
        float ang;
        switch(heuristic)
        {
            case Heuristics.H_ALPHA:
               float dist = Geometry.LongHyp(dest.getX() - succ.getX(), dest.getY() - succ.getY());
               ang = Geometry.Angle(Geometry.LongHyp(succ.getX() - start.getX(), succ.getY() - start.getY()), dist, dsg);
               h = (dist + (ang * map.get_cols()/100) * CFACTOR);
               break;
            case Heuristics.H_ALPHA2:
               if(succ.A == -1)
                   succ.A = Geometry.LongHyp(dest.getX() - succ.getX(), dest.getY() - succ.getY());
               if(pos.getParent().A == -1)
                   pos.getParent().A = Geometry.LongHyp(dest.getX() - pos.getParent().getX(), dest.getY() - pos.getParent().getY());
               ang = Geometry.Angle(Geometry.LongHyp(succ.getX() - pos.getParent().getX(), succ.getY() - pos.getParent().getY()), succ.A, pos.getParent().A);
               h = (withz?Heuristics.HeuclideanZ(succ, dest):succ.A) + (ang * CFACTOR);
               break;
            default:
               h = Heuristics.CalculateH(heuristic, succ, dest);
               break;
        }    
        return h;
    }


    public float get_g(Node pos, Node dest, boolean accumulate)
    {
        if(pos == null || dest == null)
            return -1;
	float costp = 0; // Parent cost G
        if(accumulate)
            costp = pos.getG();

        return (Geometry.LineOfSightB(map, pos, dest)?(Geometry.LongHyp(pos.getX()-dest.getX(), pos.getY()-dest.getY())+costp):-1);
    }
    
    /**
     * Allows to overwrite the path with a new path (for post-smoothing phase for example).
     * After set the new path it calculates the cost of the path.
     * @param newpath new path to store. First node is goal and last index must be start node.
     */
    protected void set_new_path(ArrayList<Node> newpath)
    {
        path = newpath;
        if(path == null || path.size() < 1)
            return;
        // Recalculate path cost
        Node pos = path.get(path.size()-1);
        pos.setG(0);
        for(int i = path.size()-2; i >= 0; i--)
        {
            path.get(i).setG(get_g(pos, path.get(i), true));
            pos = path.get(i);
        }
    }
    
    /**
     * Access to last path. For algorithms like Dijkstra you can change the goal node
     * (@see set_goal) and use this function to get the path without a new search.
     * The ArrayList could be a Node list or a double[][3] list. Generally is the first
     * case, and the second is for any-angle algorithms when use Z values during search.
     * @return The last path found or null if there is no path.
     */
    public ArrayList get_path()
    {
        if(path != null) // Path manually changed
            return path;
        if(start == null)
            return null;
        Node aux = map.get_node(goal.getX(), goal.getY()); 
        path = new ArrayList();
        path.add(aux);
        while(aux.getParent() != null && !aux.equals(start))
        {
            aux = aux.getParent();
            path.add(0,aux);
        }
        // Check last added node
        if(aux.equals(start) && path.size() > 1)
            return path;
        else
            return null;
    }
    
    /**
     * Get the cost of the last path founded.
     * @return The cost of the path or -1 if the path is not recovered yet
     * (call get_path) or there is no path.
     */
    public double get_path_cost()
    {
        if(path == null)
            return -1;
        double c = path.get(0).getG() / map.get_scale();
        if(c < pathlength)
        {
            c = 0;
            Node ant = path.get(0);
            for(int i=1; i < path.size(); i++)
            {
                c += this.get_g(ant, path.get(i), false);
                ant = path.get(i);
            }
        }
        return c;
    }

    /**
     * @return Length of the path founded. It not considers the traversals costs, only path length.
     */
    public double get_path_length()
    {
	if(path == null)
	    return -1;
        boolean oldc = this.withc;
        this.withc = false;
        pathlength = 0;
        Node ant = path.get(0);
        for(int i=1; i < path.size(); i++)
        {
            pathlength += this.get_g(ant, path.get(i), false);
            ant = path.get(i);
        }
        this.withc = oldc;
        pathlength /= map.get_scale();
        return pathlength;
    }
    
    /**
     * @return Value of total degrees of the path.
     */
    public double get_path_degrees()    
    {
	if(degrees == Double.MAX_VALUE)
	    get_heading_changes();
	return degrees;
    }
    
    /**
     * @return Number of heading changes of the path.
     * -1 if the path is not recovered yet (call get_path) or there is no path.
     */
    public int get_heading_changes()
    {
        if(path == null)
            return -1;
        float dirnew, dirant;
        beta = 0;
        hchanges = 0;
        degrees = 0;
        float dx, dy;
        Node ant = path.get(path.size()-1);
        // Set new initial orientation
        try{
         dx = path.get(path.size()-2).getX() - ant.getX();
         dy = path.get(path.size()-2).getY() - ant.getY();
        }catch(ArrayIndexOutOfBoundsException e)
         {return 0;}
        dirnew = Geometry.Angle(dx, dy, Geometry.LongHyp(dx, dy));
        dirant = dirnew;
        ant = path.get(path.size()-2);
        for(int i = path.size()-3; i >= 0; i--)
        {
            dx = path.get(i).getX() - ant.getX();
            dy = path.get(i).getY() - ant.getY();
            dirnew = Geometry.Angle(dx, dy, Geometry.LongHyp(dx, dy));
            if(dirnew > 180)
                dirnew = 360 - dirnew;
            if(dirnew != dirant)
            {
                hchanges++;
                beta += Math.abs(dirnew - dirant);
                dirant = dirnew;
            }
            ant = path.get(i);
        }
        degrees = beta;
        if(hchanges > 0)
            beta = beta / hchanges;
        return hchanges;
    }
    
    /**
     * @return The beta parameter (average angle of heading changes).
     */
    public double get_beta()
    {
        return beta;
    }
    
    /**
     * @return Number of expanded vertex.
     */
    public int get_expanded_vertex()
    {
        return expnodes;
    }
    
    /**
     * Set the expanded nodes value.
     * @param ve new expanded nodes count (> 0)
     */
    public void set_expanded_vertex(int ve)
    {
        if(ve > 0)
            expnodes = ve;
    }
    
    /**
     * Get the initial time of CPU at start of the search algorithm.
     */
    protected void start_cpu_counter()
    {
        cputime = System.currentTimeMillis();
    }
    
    /**
     * Set the total CPU time at the end of the search algorithm.
     */
    protected void end_cpu_counter()
    {
        cputime = System.currentTimeMillis() - cputime;
    }
    
    /**
     * Add specified time to the CPU time counter.
     * @param time time in milisec to add to the counter.
     */
    protected void add_cpu_time(double time)
    {
        cputime += time;
    }
    
    /**
     * Change the cpu time for the algorithm.
     * @param time the new time value (in milisec > 0)
     */
    public void set_cpu_time(double time)
    {
        if(time > 0)
            cputime = time;
    }
    
    /**
     * @return Time spent to find a solution in milisec.
     */
    public double get_cpu_time()
    {
        return cputime;
    }

    /**
     * @return True if the algorithm is any-angle. False otherwise.
     */
    public boolean is_any_angle()
    {
	return anyangle;
    }
    
    /**
     * @return A string with the information of map and the path planning algorithm.
     */
    public String toString()
    {
	String temp = map.toString() + "\nPath-planning algorithm: " + name + 
                (anyangle? "\n Any-angle":"") +
                (withz? "\n With elevation" : "" ) +
                (withc? "\n With traversal costs":"") +
                "\n Alpha weight: " + SearchMethod.CFACTOR ;
        if(start != null)
            temp += "\n Initial point: " + start.getXYZ();
        else
            temp += "\n No initial point set";
        if(goal != null)
            temp += " \n Goal point: " + goal.getXYZ();
        else
            temp += " \n No goal point set";
        return temp;
    }
    
    /**
     * @return A string with the number of steps and cost of the path.
     */
    public String path_info()
    {
        return "#p-length: " + get_path_length()+ "\n" +
        (withc?"#p-cost  : " + get_path_cost() + "\n" : "") +
               "#h-change: " + get_heading_changes() + "\n" +
               "#v-expans: " + expnodes + "\n" +
               "#cpu-time: " + cputime + "\n" +
               "#beta-avg: " + beta + "\n" +
               "#degrees : " + degrees + "\n" +
               "#nº-steps: " + (path!=null?path.size():0)+ "\n" +
  (reexpansion?"#reexpans: " + reexpnodes + "\n" : "");
                //"#perform : " + get_performance() +
    }
    
    /** 
     * Print XYZ path nodes.
     */
    public void print_path()
    {
	if(path != null)
        for(int i = path.size()-1; i >= 0; i--)
            System.out.println(((Node)path.get(i)).getXYZ());
    }
    
}
