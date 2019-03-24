package PathPlanning;

import java.util.*;


/**
 * Implements the basic Theta* algorithm to search a path in a DEM.
 * The algorithm is described in  <i>Theta*: Any-Angle Path Planning on Grids. Kenny Daniel et al., 2010</i>.
 * @author Pablo Muñoz
 */

public class Thetastar extends SearchMethod {
    
    /** Open nodes list, sorted by F. */
    private List<Node> open;
    /** Closed nodes table. */
    private Hashtable closed;
    
    
    /**
     * Constructor for APThetastar class.
     * @param dem terrain info.
     * @param init start node.
     * @param end goal node.
     * @param heur heuristic used for search.
     * @param withz true for use Z values.
     * @param withc true for use transversal costs.
     */
    public Thetastar(Map dem, Node init, Node end, short heur, boolean withz, boolean withc)
    {
        super("Basic Theta*", dem, init, end, heur, withz, withc, true);
	open = new ArrayList();
	closed = new Hashtable();
        // Change tie breaking flag
        Node.ChangeTieBreakingFlag();
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
     * Search a path using the basic Theta* algorithm.
     * @return True if a path is found or false if no path found.
     */
    @Override
    public boolean search()
    {
        if(!check_valid_data())
            return false;
        ArrayList<Node> succlist = new ArrayList();
        
        // Set initial state
        start.setParent(start);
        map.get_node(start.getX(), start.getY()).setParent(start);
        map.get_node(start.getX(), start.getY()).setF(0, get_h(start, start, goal));
        open.add(map.get_node(start.getX(), start.getY()));
        Node pos;
        // Auxiliary variables
        Node aux;
        
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
                    updateVertex(pos, aux);
            }// End of succesor check
            // Sort open list
            Collections.sort(open);

        } // Open list is empty, no path found
        end_cpu_counter();
        return false;
    }
    
    /**
     * Select the path from pos.parent-pos-succ according to the Basic Theta* algorithm.
     * Path 1 is A* path and Path 2 is any angle path.
     * @param pos actual position.
     * @param succ successor to test.
     */
    private void updateVertex(Node pos, Node succ)
    {
        float hsuc, gsuc;
        int x = succ.getX();
        int y = succ.getY();

        if(heuristic == Heuristics.H_ALPHA2) 
        {
            if(succ.A == -1)
                succ.A = Geometry.LongHyp(goal.getX() - x, goal.getY() - y);
            if(pos.getParent().A == -1)
                pos.getParent().A = Geometry.LongHyp(goal.getX() - pos.getParent().getX(), goal.getY() - pos.getParent().getY());
            float ang = Geometry.Angle(Geometry.LongHyp(x - pos.getParent().getX(), y - pos.getParent().getY()), succ.A, pos.getParent().A);
                hsuc = succ.A + (ang * map.get_cols()/100);
                gsuc = get_g(pos.getParent(), succ, true);
        }
        else // Basic Theta*
        {
            hsuc = get_h(pos, succ, goal);
            gsuc = get_g(pos.getParent(), succ, true);
        }
        
        if(gsuc > 0)
        {
            // Path 2
            if(gsuc < succ.getG())
            {
                map.get_node(x, y).setF(gsuc, hsuc);
                map.get_node(x, y).setParent(pos.getParent());
                // Remove succesor from open if is contained and insert succesors in open list
                open.remove(map.get_node(x, y));
                open.add(map.get_node(x, y));
            }
        }
        else
        {
            // Path 1 (A*)
            gsuc = get_g(pos, succ, true);
            if(gsuc < succ.getG() || (withc && gsuc <= succ.getG()))
            {
                map.get_node(x, y).setF(gsuc, hsuc);
                map.get_node(x, y).setParent(pos);
                // Remove succesor from open if is contained and insert succesors in open list
                open.remove(map.get_node(x, y));
                open.add(map.get_node(x, y));
            }
        }
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
