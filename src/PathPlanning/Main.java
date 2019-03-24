package PathPlanning;

import java.util.ArrayList;


/**
 * Main class. Read arguments and executes path planning algorithm.
 * @author Pablo Muñoz
 */
public class Main {

    
    public static boolean isNumeric(String s) 
    {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }  
        
    /**
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        try{
         
	/******************************
         *  TASK PLANNER CONNECTION
         ******************************/ 
	 if((args.length == 3 || args.length == 4) && args[0].charAt(0) == '.')
	 {
	     double timestart = System.currentTimeMillis();
	     TaskPlannerConnect pathsearch = new TaskPlannerConnect();
	     if(!pathsearch.initialize(args))
		 System.exit(-1);
	     float out = pathsearch.runSearch();
	     pathsearch.close();
	     if(out > 0)
	     {
		 //System.out.println("Accumulated cost: "+out);
		 System.out.println("#CPU-TIME: "+(System.currentTimeMillis()-timestart));
		 System.exit(0);
	     }
	     else
	     {
		 System.out.println("Search failed.");
		 System.exit(-1);
	     }
	 }
       
	if(args.length < 5)
	{
            System.out.print("Usage:");
            System.out.println("\n>For file generation:");
            System.out.println("  -g{z,c,r} n-cols n-rows value outfile\n");
            System.out.println("     z for DEM generation with 'value' as number of iteration for hill algorithm.");
            System.out.println("     c for cost file, 'value' is the percentage of obstacles ");
            System.out.println("       (less 40% to guarantee convergence of the algorithm.");
            System.out.println("     r for generate indoor map using square patterns r0-r5, 'value' is the size of");
            System.out.println("       the side for the patterns.");
            System.out.println("\n>To search a path:");
            System.out.println("  z-file c-file Xi Yi Xg Yg -alg [n] [-z[q]] [-c] pathfile [-w[s]]\n");
            System.out.println("     -z specifies to use altitude in search. Uses lineal interpolation for");
            System.out.println("        any-angle algorithms. Using -zq changes to quadratic interpolation.");
            System.out.println("     -c specifies to use transversal costs in search.");
            System.out.println("     n multiplicative factor for alpha weight (olny b/v algorithms, def=1).");
            System.out.println("     -alg can be: -a -> A*, -p -> A*PS, -d -> Dijkstra, -t -> Basic Theta*");
            System.out.println("                  -s -> S-Theta*, -b -> A*PS ALPHAp, -v -> Basic Theta* ALPHAp");
            System.out.println("                  -n -> 3Dana");
            System.out.println("     -w[s] show a window with map and path representation, s for scale zoom [1,50].");
            System.out.println("     When -z is used, it is possible to specify an altitude scaling factor and a maximum allowed slope ([0º, 45º]).");
            System.out.println("     Provide both factors after the -z parameter. If angle is negative, it disable the option: -z Scale Slope");
            System.exit(-1);
	}
	// Main data
	int nextarg = -1;
        Map map;
        
        /******************************
         *  GENERATE Z_FILE OR C_FILE
         ******************************/
        String aux = args[++nextarg];
        if(aux.equals("-gz") || aux.equals("-gc") || aux.equals("-gr"))
	{
            int cols, rows;
            cols = Integer.valueOf(args[++nextarg]);
            rows = Integer.valueOf(args[++nextarg]);
            int maxz = Integer.valueOf(args[++nextarg]);
            if(cols < 1 || rows < 1 || maxz < 0) // Check files/rows value
            {
                System.out.println("Invalid col/row/z/c value (must be > 0, z/c >=0) [" + cols + "," + rows + "](" + maxz + ")");
                System.exit(-1);
            }
            switch(aux.charAt(2))
            {
                case 'z':
                    FileManager.GenerateZfile(args[++nextarg], cols, rows, maxz);
                    break;
                case 'c':
                    FileManager.GenerateCfile(args[++nextarg], cols, rows, maxz);
                    break;
                case 'r':
                    FileManager.GenerateRfile(args[++nextarg], cols, rows, maxz);
                    break;
                default:
                    System.out.println("Invalid generation method.");
                    System.exit(-1);
            }
            System.exit(0);
        }
        
        /****************
         *  SEARCH PATH
         ****************/
        if(args.length < 7)
        {
            System.out.println("Too few parameters to search a path.");
            System.exit(-1);
        }
        // Open the Z-file or create a 10 random altitude DEM parsing the arguments
        try{
            // Less than 3 arguments valid with z-file
            if(args.length < 3)
                throw new java.lang.NumberFormatException();
            map = new Map(Integer.parseInt(args[nextarg]), Integer.parseInt(args[++nextarg]), 10, 10, false);
        }catch(NumberFormatException notNumber){
            // DEM file specified
            String cfile = "";
            if(args.length >= 8) // 8 args or more implies cost file
            {
                cfile = args[1];
                nextarg++;
            }
            map = new Map(args[0], cfile);
        }
        // Check if DEM file is valid
        if(!map.valid_dem())
            System.exit(-1);
        
        // Get waypoints
        ArrayList<int[]> wp = new ArrayList<int[]>();
        try{
         while(isNumeric(args[nextarg+1]) && isNumeric(args[nextarg+2]))
         {
            int wx = Integer.valueOf(args[++nextarg]);
            int wy = Integer.valueOf(args[++nextarg]);
            wp.add(new int[]{wx,wy});
         }
        }catch(Exception e)
        {
            System.out.println("Check arguments for initial and goal position. " + e.toString());
            System.exit(-1);
        }
        int wj = 0;
        while(wj < wp.size())
        {
            int wx, wy;
            wx = wp.get(wj)[0];
            wy = wp.get(wj)[1];
            if(!map.valid_pos(wx, wy))
            {
                System.out.println("Invalid waypoint ("+wx+","+wy+"); X and Y must be greater or equal to 0 and less than cols or rows.");
                System.out.println("Map of ["+map.get_cols()+"x"+map.get_rows()+"]");
                wp.remove(wj);
            }
            else if(map.get_tcost(wx, wy) > Map.MAX_COST)
            {
                System.out.println("Invalid waypoint ("+wx+","+wy+"): is an obstacle.");
                wp.remove(wj);
            }
            else 
            if(map.get_tcost(wx, wy) > Map.MAX_COST)
            {
                System.out.println("Invalid waypoint ("+wx+","+wy+"): is an obstacle!");
                wp.remove(wj);
            }
            else
                wj++;
        }
        if(wp.size() < 2)
        {
            System.out.println("Not enough waypoints provided!\n");
            System.exit(-1);
        }
        
        // Read search algorithm
        if(args[nextarg+1].length() < 2)
        {
            System.out.println("Invalid search algorithm: "+args[nextarg+1]);
            System.exit(-1);
        }
        char alg = args[++nextarg].charAt(1);
        // Check if want to use the Z and/or C values
        boolean withz = false;
        boolean withc = false;
	float delta = Float.NaN;
        short heur = Heuristics.H_EUCLIDEAN;
        try{
	 try{
          delta = Float.parseFloat(args[nextarg+1]);
         }catch(NumberFormatException nfe){}
	 if(!Float.isNaN(delta))
         {
	     SearchMethod.changeCfactor(delta);
             nextarg++;
         }
         if(args[nextarg+1].startsWith("-c") || args[nextarg+2].startsWith("-c"))
         {
             withc = true;
             if(args[nextarg+1].length()>2)
                 try{
                     short safetyMargin = (short)Integer.parseInt(args[nextarg+1].substring(2));
                     if(!map.expand_costmap(safetyMargin))
                         System.err.println("Failed to apply safety marging (invalid value? "+safetyMargin+")");
                 }catch(NumberFormatException nfe){ }
         }
         if(args[nextarg+1].startsWith("-z") || args[nextarg+2].startsWith("-z"))
         {
             withz = true;
             heur = Heuristics.H_EUCLIDEAN_Z;
         }
        }catch(ArrayIndexOutOfBoundsException a){}
        if(withz) nextarg++;
        if(withc) nextarg++;
        // Check map altitude scaling
        if(withz)
        {
         delta = Float.NaN;
         try{
          delta = Float.parseFloat(args[nextarg+1]);
         }catch(NumberFormatException nfe){}
	 if(!Float.isNaN(delta))
         {
	     map.scale_dem(delta);
             System.out.println("Changed DTM scale with x"+delta);
             nextarg++;
         }
   
        }

        // <<<<< BEGIN SEARCH >>>>>
        SearchMethod algorithm;
        int xs = wp.get(0)[0];
        int ys = wp.get(0)[1];
        int xg = wp.get(1)[0];
        int yg = wp.get(1)[1];
        switch(alg)
        {
            case 'a': // Base A* with octile distance as heuristic function
                algorithm = new Astar(map, map.get_node(xs, ys), map.get_node(xg, yg), Heuristics.H_OCTILE, withz, withc);
                break;
            case 'r': // Base A* with vertex re-expansion
                algorithm = new Astar(map, map.get_node(xs, ys), map.get_node(xg, yg), Heuristics.H_OCTILE, withz, withc);
                break;
            case 'p': // A* Post Processed
                algorithm = new Astar(map, map.get_node(xs, ys), map.get_node(xg, yg), heur, withz, withc);
                break;
            case 'd': // Dijkstra algorithm
                algorithm = new Dijkstra(map, map.get_node(xs, ys), map.get_node(xg, yg), heur, withz, withc);
                break;
            case 't': // Basic Theta* and G-Theta* when altitude is used in search process
                algorithm = new Thetastar(map, map.get_node(xs, ys), map.get_node(xg, yg), heur, withz, withc);
                break;
            case 'u': // 
                algorithm = new Thetastar(map, map.get_node(xs, ys), map.get_node(xg, yg), Heuristics.H_ALPHA2, withz, withc);
                algorithm.change_name("S-Theta*");
                break;
            case 's': // S-Theta* for low steering
                algorithm = new STheta(map, map.get_node(xs, ys), map.get_node(xg, yg), Heuristics.H_EUCLIDEAN, withz, withc);
                break;
            case 'b': // A*PS with greedy heuristic
                algorithm = new Astar(map, map.get_node(xs, ys), map.get_node(xg, yg), Heuristics.H_ALPHA, withz, withc);
                break;
            case 'v': // Basic Theta* with greedy heuristic
                algorithm = new Thetastar(map, map.get_node(xs, ys), map.get_node(xg, yg), Heuristics.H_ALPHA, withz, withc);
                algorithm.change_name("Basic Theta* H-alpha");
                break;
	    default:
                algorithm = new Astar();
                System.out.println("Invalid search algorithm: " + alg);
                System.exit(-1);
        }
        
        System.out.println(algorithm.toString()+"\n");
        double cputime = 0;
        int expnodes = 0;
        int wi = 0;
        ArrayList path = new ArrayList();
        do // Search for wp paths
        {
            xs = wp.get(wi)[0];
            ys = wp.get(wi)[1];
            xg = wp.get(wi+1)[0];
            yg = wp.get(wi+1)[1];
            algorithm.restart_search(map.get_node(xs, ys), map.get_node(xg, yg));
            System.out.println("Searching path from ("+xs+","+ys+") to ("+xg+","+yg+")\n");
            if(alg != 'r')
                algorithm.search();
            else
            {
                ((Astar)algorithm).search_rex();
                algorithm.change_name("A* with re-expansion");
            }
            if(alg == 'd')
                ((Dijkstra)algorithm).get_path(map.get_node(xg, yg));
            if(alg == 'p' || alg == 'b') // Post process phase when required
                ((Astar)algorithm).search_ps();
            if(alg == 'b')
                algorithm.change_name("A*PS H-alpha");

            if(algorithm.get_path() == null) // No path for a pair of waypoints
            {
                System.out.println("No path found from ("+xs+","+ys+") to ("+xg+","+yg+").\n");
                System.exit(-1);
            }
            for(int p=0; p<algorithm.get_path().size(); p++)
                if(path.isEmpty() || !((Node)algorithm.get_path().get(p)).equals((Node)path.get(path.size()-1)))
                    path.add(algorithm.get_path().get(p));
            
            cputime += algorithm.get_cpu_time();
            expnodes += algorithm.get_expanded_vertex();
            wi++;
        }while(wi < wp.size()-1);
        
        // Output file with path
        String outfile = args[++nextarg];
        algorithm.set_new_path(path);
        algorithm.set_cpu_time(cputime);
        algorithm.set_expanded_vertex(expnodes);
        aux = algorithm.path_info();
        FileManager.WritePathFile(outfile, path, aux+"\nPATH:");
        System.out.println("Output file with found path is " + args[nextarg]);
        
        
        System.exit(0);
        
     //----------------------------------/
     }catch(ArrayIndexOutOfBoundsException a){
        System.out.println("Invalid argument index "+a.toString());
        System.exit(-1); 
     }
     catch(Exception e){
        System.out.println(e.toString());
        System.exit(-1);
     }
    }
}
