package PathPlanning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements the Dijkstra algorithm to search a path in a DEM.
 * @author Pablo Muñoz
 */
  public class Dijkstra extends SearchMethod {
   
    /** Open nodes list, sorted by F. */
    private List<Node> open;
  
    /**
     * Constructor. Create the open vector with all nodes.
     * @param dem the DEM info.
     * @param init start node.
     * @param end goal node.
     * @param heur not used in Dijkstra algorithm.
     * @param withz true for use Z values.
     * @param withc true for use transversal costs.
     */
    public Dijkstra(Map dem, Node init, Node end, short heur, boolean withz, boolean withc)
    {
        super("Dijkstra", dem, init, end, heur, withz, withc, false);
	    open = new ArrayList();
        // Copy all nodes to the open table and set the cost to ~INF.
        for(int j = 0; j < map.get_rows(); j++)
            for(int i = 0; i < map.get_cols(); i++)
                if(!map.get_node(i, j).isObstacle() && map.get_tcost(i, j) <= Map.MAX_COST)
                {
                    map.get_node(i, j).setF(0,Float.MAX_VALUE);
                    map.get_node(i, j).setParent(null);
                    open.add(map.get_node(i, j));
                }
    }
    
    @Override
    /**
     * Clear internal data of the algorithm.
     */
    protected void clear_internal_data()
    {
	open.clear(); // References in open list lost in DEM clean (new nodes)
	for(int j = 0; j < map.get_rows(); j++)
            for(int i = 0; i < map.get_cols(); i++)
            {
                map.get_node(i, j).setF(0,Float.MAX_VALUE);
                map.get_node(i, j).setParent(null);
                open.add(map.get_node(i, j));
            }
    }
    
    /**
     * Search a path using the Dijkstra algorithm.
     * @return True if a path is found or false if no path found.
     */
    @Override
    public boolean search()
    {
        if(!check_valid_data())
            return false;
        
        // Set initial state
        Node pos = map.get_node(start.getX(), start.getY());
        pos.setF(0,0);
        pos.setParent(map.get_node(pos.getX(), pos.getY()));
        open.remove(pos);
        open.add(0, pos);
        // Auxiliary variables
        float gsuc;
        int x=0, y=0;
        ArrayList<Node> succ = new ArrayList();
        
        // Dijkstra expansion
        start_cpu_counter();
        while(!open.isEmpty())
        {
            // Get better node
            pos = open.remove(0);
            expnodes++;

            // Generate succesors for actual point
            succ = map.get_succesors_without_obstacles(pos);
            for(int i=0; i < succ.size(); i++)
            {
                x = succ.get(i).getX();
                y = succ.get(i).getY();
                gsuc = get_g(map.get_node(pos.getX(), pos.getY()), map.get_node(x, y), true);
                // If new path to node is more expensive, not modify
                if(gsuc > 0 && map.get_node(x, y).getG() > gsuc)
                {
                    // Replace node data
                    map.get_node(x, y).setParent(map.get_node(pos.getX(), pos.getY()));
                    map.get_node(x, y).setF(gsuc,0);
                }
            }// End of succesor check
            // Sort open list
            Collections.sort(open);
        }
        // Search finish
        end_cpu_counter();
	    if(map.get_node(goal.getX(), goal.getY()).getParent() != null)
	        return true;
	    else
	        return false;
    }
    
    /**
     * Get the path from the initial node to another destination node.
     * @param dest new destination node.
     * @return The new path or null if there is no path or dest node is invalid.
     */
    public ArrayList get_path(Node dest)
    {
        if(set_goal(dest))
            return get_path();
        else
            return null;
    }
    
    /**
     * Get the cost to reach a node from the initial node.
     * @param dest the destination node.
     * @return The cost of the path or -1 if no path available.
     */
    public float get_path_cost(Node dest)
    {
	// If a node has not got a parent, this node is unreachable
        if(map.valid_pos(dest.getX(), dest.getY()) && map.get_node(dest.getX(), dest.getY()).getParent() != null)
            return map.get_node(dest.getX(), dest.getY()).getG();
        else
            return -1;
    }
    
    /**
     * Print the open list.
     */
    private void print_open()
    {      
        System.out.println("Open:");
        for(int i=0; i < open.size(); i++)
            System.out.println("    " + open.get(i).toString());
        System.out.println();
    }
    
  }
