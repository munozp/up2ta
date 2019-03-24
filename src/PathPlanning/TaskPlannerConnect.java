package PathPlanning;

import java.io.*;
import java.util.ArrayList;

/**
 * Connection with a task planner using a modified FF PDDL planner.
 * @author Pablo Muñoz
 */
public class TaskPlannerConnect {
    
    /** Selected path-planning algorithm. */
    private SearchMethod pathplanner;
    private boolean postprocess = false;
    /** Heuristic employed. */
    private short heur;
    /** Digital Elevation Map. */
    private Map dem;
    /** Last path found. */
    ArrayList path;
    /** Complete path. */
    ArrayList fullpath;
    /** Required for initialization. */
    Node initnode;
    /** Path for pipe to read task planner commands. */
    private final String ReadFromFF = "/tmp/tuberia-ff-astar";
    /** Path for pipe to write information to the task planner. */
    private final String WriteToFF = "/tmp/tuberia-astar-ff";
    /** Read stream from FF. */
    private FileReader pipeRead;
    /** Write stream to FF. */
    private FileWriter pipeWrite;
    /** Commands send by FF: */
    private static final short HEUR = '0';
    private static final short PATH = '1';
    private static final short EXIT = '2';
    private static final short COST = '3';
    /** Format of the points: */
    private static final char CAS = 'C';
    private static final char DEL = '_';
    private static final char END ='\0';
    private static final String MOVETO = "MOVETO";
    private static final String ROBOT = ""; //If no empty put a space after!
    private java.text.DecimalFormat formatf; 
    
    
    public TaskPlannerConnect()
    {
    }
    
    /** 
     * Read parameters and open files. 
     * @return True if data is valid, false if unknown algorithm or cannot open a file.
     */
    public boolean initialize(String args[])
    {
        FileManager.disableLoadIndicator = true;
	// Load dem
	dem = new Map(args[1], args[2]);
	if(!dem.valid_dem() && !dem.valid_cost_map())
	    return false;
	char alg = args[0].charAt(1);
	boolean withc = false;
	if(args[0].length() > 2 && args[0].charAt(2) == 'c')
	    withc = true;
        int sp = (withc?1:0);
        if(args[0].length() > (2+sp) && Character.isDigit(args[0].charAt(2+sp)))
            dem.expand_costmap((short)Integer.parseInt(String.valueOf(args[0].charAt(2+sp))));
        for(int i=0; i<dem.get_cols(); i++)
        {
            for(int j=0; j<dem.get_rows(); j++)
                if(dem.get_tcost(i, j) < Map.MAX_COST && !dem.get_node(i, j).isObstacle())
                {
                    initnode = dem.get_node(i,j);
                    break;
                }
            if(initnode != null)
                break;
        }
	// Set the heuristic and create the path-planning algorithm
	switch(alg)
	{
	    case 'a': // A*
		heur = Heuristics.H_OCTILE;
	        pathplanner = new Astar(dem, initnode, initnode, heur, false, withc);
		break;
            case 'p': // A*PS
		heur = Heuristics.H_EUCLIDEAN;
	        pathplanner = new Astar(dem, initnode, initnode, heur, false, withc);
                pathplanner.change_mode(true);
                postprocess = true;
		break;
	    case 'd': // Dijkstra
		heur = Heuristics.H_OCTILE;
		pathplanner = new Dijkstra(dem, initnode, initnode, heur, false, withc);
		break;
	    case 't': // Basic Theta*
		heur = Heuristics.H_EUCLIDEAN;
		pathplanner = new Thetastar(dem, initnode, initnode, heur, false, withc);
		break;
	    case 'v': // Greedy Basic Theta* 
		heur = Heuristics.H_ALPHA;
		pathplanner = new Thetastar(dem, initnode, initnode, heur, false, withc);
                pathplanner.change_name("Greedy Basic Theta*");
		break;
	    case 's': // S-Theta*
		heur = Heuristics.H_ALPHA2;      
		pathplanner = new Thetastar(dem, initnode, initnode, heur, false, withc);
                pathplanner.change_name("S-Theta*");
		break;
	    case 'A': // A* with Z
		heur = Heuristics.H_OCTILE_Z;
		pathplanner = new Astar(dem, initnode, initnode, heur, true, withc);
		break;
	    case 'D': // Dijkstra with Z
		heur = Heuristics.H_OCTILE_Z;
		pathplanner = new Dijkstra(dem, initnode, initnode, heur, true, withc);
		break;
	    default:
		System.out.println("Invalid path-planning algorithm: "+alg);
		return false;
	}
	// Open pipes
        try{
	    String abspath = new File("").getAbsolutePath();
	    pipeWrite= new FileWriter(WriteToFF);
	    pipeRead = new FileReader(ReadFromFF);
        }catch(FileNotFoundException nf){
            System.out.println("Cannot open file: " + nf.toString());
	    return false;
        }catch(IOException ioe){
            System.out.println("Input/output error: " + ioe.toString());
	    return false;
        }
	// Define formating for H and G values
	java.text.DecimalFormatSymbols sim = new java.text.DecimalFormatSymbols();
	sim.setGroupingSeparator(',');
	sim.setDecimalSeparator('.');
        formatf = new java.text.DecimalFormat("######.######",sim);
	formatf.setGroupingSize(9);
	formatf.setMinimumIntegerDigits(6);
	return true; // OK
    }
    
    /** 
     * Perform the communication between path-planning and task planner. 
     * @return the accumulate cost of the search if search is OK or less or equal 0 if it fails.
     */
    public float runSearch()
    {
	char[] readpoint = new char[10];
	char[] readoption = new char[4];
	String writebuff;
	int nbread;
	int caspos, delpos;
	float cost = 0;
	float deg = 0;
	float partialcost = 0;
	float partialdeg = 0;
	float dirant = 0;
	Node start = new Node(0,0,0);
        Node goal = new Node(0,0,0);
	fullpath = new ArrayList();
	SearchMethod greedy; // For cost
        if(pathplanner.is_any_angle())
	    greedy = new Thetastar(dem, initnode, initnode, Heuristics.H_ALPHA, false, false);
	else
	    greedy = new Astar(dem, initnode, initnode, Heuristics.H_ALPHA, false, false);
	do{ // Executes until EXIT command
	try{
	    nbread = pipeRead.read(readoption);
	    if(readoption[0] == EXIT)
	    {
		System.out.println("#pathcost: "+cost);
		System.out.println("#degrees_: "+deg);
		return cost;
	    }
	    
	    // Read start point
	    nbread = pipeRead.read(readpoint);
	    writebuff = String.valueOf(readpoint);
	    caspos = writebuff.indexOf(CAS);
	    delpos = writebuff.indexOf(DEL);
	    start = new Node(Integer.valueOf(writebuff.substring(caspos+1, delpos)), dem.get_rows()-Integer.valueOf(writebuff.substring(delpos+1, writebuff.indexOf(END))), 0); 
	    start.setParent(start); // For alpha-based heuristics
	    // Confirm start point
	    pipeWrite.write(writebuff);
	    pipeWrite.flush();
	    // Read goal point
	    nbread = pipeRead.read(readpoint);
	    writebuff = String.valueOf(readpoint);
	    caspos = writebuff.indexOf(CAS);
	    delpos = writebuff.indexOf(DEL);
	    goal = new Node(Integer.parseInt(writebuff.substring(caspos+1, delpos)), dem.get_rows()-Integer.parseInt(writebuff.substring(delpos+1, writebuff.indexOf(END))), 0);

	    switch(readoption[0])
	    {
		case HEUR: // Calculate heuristic
		    // Confirm goal point
		    pipeWrite.write(writebuff);
		    pipeWrite.flush();
		    if(pathplanner.is_any_angle())
		        writebuff = formatf.format(Heuristics.Heuclidean(start, goal));
		    else
			writebuff = formatf.format(Heuristics.Hoctile(start, goal));
		    pipeWrite.write(writebuff);
		    pipeWrite.flush();
		    break;
		case PATH: // Perform search
		case COST:
		    if(!goal.equals(start))
		    {
		        if(readoption[0] == PATH)
			{
			    pathplanner.restart_search(start, goal);
			    if(!pathplanner.search())
				return -1;
                            if(postprocess)
                                ((Astar)pathplanner).search_ps();
			    path = pathplanner.get_path();
			    partialcost = (float)pathplanner.get_path_cost();
			    partialdeg = (float)pathplanner.get_path_degrees();
			    int dx = ((Node)path.get(path.size()-2)).getX() - ((Node)path.get(path.size()-1)).getX();
			    int dy = ((Node)path.get(path.size()-2)).getY() - ((Node)path.get(path.size()-1)).getY();
			    float dirinit = Geometry.Angle(dx, dy, Geometry.LongHyp(dx, dy));
			    float dirchange = Math.abs(dirinit - dirant);
			    if(dirchange > 180)
				dirchange = 360 - dirchange;
			    partialdeg += dirchange;
			    dx = ((Node)path.get(0)).getX() - ((Node)path.get(1)).getX();
			    dy = ((Node)path.get(0)).getY() - ((Node)path.get(1)).getY();
			    dirant = Geometry.Angle(dx, dy, Geometry.LongHyp(dx, dy));
			}
			else
			{
			    greedy.restart_search(start, goal);
			    if(!greedy.search())
				return -1;
                            greedy.get_path();
			    partialcost = (float)greedy.get_path_cost();
			}
		    }
		    else // Same points
		    {
			partialcost = 0;
			partialdeg = 0;
			path = new ArrayList();
		    }
		    if(readoption[0] == PATH)
		    {
			for(int i=path.size()-1; i>=0; i--)
			    fullpath.add(0,path.get(i));
                        print_moveto_path();
			cost += partialcost;
			deg += partialdeg;
		    }
		    // Confirm goal point
		    pipeWrite.write(writebuff);
		    pipeWrite.flush();
		    if(readoption[0] == COST) // Send cost to planner
		    {
			writebuff = formatf.format(partialcost);
			pipeWrite.write(writebuff);
			pipeWrite.flush();
		    }
		    break;
		default:
		    System.out.println("Invalid command received.");
	    }

	}catch(IOException ioe){
            System.out.println(ioe.toString()); 
            return -1;     }
         catch(Exception e){
             System.out.println(e.toString()+"\n Option "+readoption[0]+" "+start.getXYZ()+" > "+goal.getXYZ());
             return -1;    }
	}while(true);
    }
    
    /**
     * Close pipes.
     */
    public void close()
    {
	try{
	 pipeRead.close();
	 pipeWrite.close();
	}catch(IOException ioe){System.out.println("Problem closing pipes: "+ioe.toString());}
    }
    
    public void print_moveto_path()
    {
        Node o,d;
	if(path != null)
        for(int i = path.size()-2; i >= 0; i--)
        {
            o = (Node)path.get(i+1);
            d = (Node)path.get(i);
            // FORMAT IS CY_X!!!
            System.out.print(MOVETO+" "+ROBOT+CAS+o.getX()+DEL+(dem.get_rows()-o.getY())+" "+CAS+d.getX()+DEL+(dem.get_rows()-d.getY())+END);
            if(i>0)
                System.out.println();
        }
    }


}
