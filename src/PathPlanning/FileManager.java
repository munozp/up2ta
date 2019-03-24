package PathPlanning;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Set of functions to read and write files with DEM info and to write a path. All in a format readable from gnuplot.
 * @author Pablo Muñoz
 */
public abstract class FileManager {
    
    /** Allows to disable the load indicator. */
    public static boolean disableLoadIndicator = false;

    /**
     * Read a DTM model from the given file.
     * @param file the path to an ASCII or DTM file
     * @return Null if there is any problem or and matrix with the DEM otherwise.
     */
    public static Node[][] ReadZfile(String file)
    {
        FileReader fr = null;
        try{
            fr = new FileReader(new File(file).getAbsolutePath());
            BufferedReader zfile = new BufferedReader(fr);
            if(zfile.readLine().contains("PDS"))
                return ReadDTMfile(file);
            else
                return ReadZASCIIfile(file);
        }catch(Exception e){
            if(!FileManager.disableLoadIndicator)
            System.out.println("Failed to read DTM from file "+file);
        }
        if(fr != null)
         try{
            fr.close();
         }catch(IOException ex){ }
        return null;
    }
    
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for(int i=0; i<len; i+=2)
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        return data;
    }
    
    /**
     * Read the Z values from a DTM file and store the information into a matrix.
     * @param file Relative path to the DTM file to be read.
     * @return Null if there is any problem or and DEM map otherwise.
    */
    public static Node[][] ReadDTMfile(String file) {
        String abspath = new File(file).getAbsolutePath();
       
        try{
            int cols = 0, rows = 0;
            String line = "";
            String keyword = "";
            String value = "";
            int recordBytes = 0;
            int readOffset = 0;
            float min = 0;
            float max = 1;
            // Read file properties
            FileReader fr = new FileReader(abspath);
            BufferedReader zfile = new BufferedReader(fr);
            while(!line.trim().equals("END")) 
            {
                line = zfile.readLine();
                if(line == null)
                    break;
                line = line.replace('"', ' ');
                if(line.trim().equals(""))
                    continue;
                if(line.indexOf("=") > 0) {
                    keyword = line.substring(0, line.indexOf("=")).trim();
                    value = line.substring(line.indexOf("=") + 1, line.length()).trim().toUpperCase();
                }
                else
                    continue;
                if(value.length() == 0)
                    continue;
                if(keyword.equals("LINES"))
                    rows = Integer.parseInt(value);
                else if(keyword.equals("LINE_SAMPLES"))
                    cols = Integer.parseInt(value);
                else if(keyword.equals("RECORD_BYTES"))
                    recordBytes = Integer.parseInt(value);
                else if(keyword.equals("VALID_MINIMUM"))
                    min = Float.parseFloat(value);
                else if(keyword.equals("VALID_MAXIMUM"))
                    max = Float.parseFloat(value);
                else if(keyword.equals("^IMAGE")) 
                    readOffset = (Integer.parseInt(value)-1) * recordBytes;
            }//while
            
            try{
                zfile.close();
            }catch(Exception e){}
            // Read file data
            Node temp[][] = new Node[cols][rows];
            float z = 0;
            float range = max - min;
            float zant = -1;
            byte[] buffer = new byte[4];
            byte[] ignoreByte = hexStringToByteArray("FF7FFFFB");
            float ignoreFloat = java.nio.ByteBuffer.wrap(ignoreByte).order(java.nio.ByteOrder.BIG_ENDIAN).getFloat();
            FileInputStream zdata = new FileInputStream(abspath);
            zdata.skip(readOffset);
            if(!FileManager.disableLoadIndicator)
                System.out.println();
            
            for(int j = 0; j < rows; j++)
            {
                for(int i = 0; i < cols; i++)
                {
                    zdata.read(buffer);
                    z = java.nio.ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getFloat();
                    if(z == ignoreFloat)
                        z = Float.MIN_VALUE;
                    temp[i][j] = new Node(i, j, z);
                    zant = z;
                }
                if(!FileManager.disableLoadIndicator)
                {
                    System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
                    System.out.print("DTM Readed: "+(j*100/rows)+"%");
                }
            }
            zdata.close();
            return temp;
            
        }catch(FileNotFoundException nf){
            if(!FileManager.disableLoadIndicator)
            System.out.println("\nNo input DTM file found: " + file);
        }catch(IOException ioe){
            if(!FileManager.disableLoadIndicator)
            System.out.println("\nInput/output error: " + ioe.toString());
        }
        return null; // Fail to charge
    }
    
    
    /**
     * Read the Z values from an ASCII file and store the information into a matrix.
     * @param file Relative path to the ASCII file to be read.
     * @return Null if there is any problem or and DEM map otherwise.
    */
    public static Node[][] ReadZASCIIfile(String file) {  
        String abspath = new File(file).getAbsolutePath();
       
        try{
            FileReader fr = new FileReader(abspath);
            BufferedReader zfile = new BufferedReader(fr);

            int cols, rows = 1;
            // Read first row to get number of cols
            StringTokenizer st = new StringTokenizer(zfile.readLine());
            cols = st.countTokens();
            while(zfile.readLine() != null)
                rows++;
            // Reset buffer reader and create the array
            fr = new FileReader(abspath);
            zfile = new BufferedReader(fr);
            Node temp[][] = new Node[cols][rows];

            // Read file
            if(!FileManager.disableLoadIndicator)
                System.out.println();
            float z;
            //for(int j = (rows-1); j >=0; j--)
            for(int j = 0; j < rows; j++)
            {
                st = new StringTokenizer(zfile.readLine());
                for(int i = 0; i < cols; i++)
                    if(st.hasMoreTokens())
                    {
                        z = Float.valueOf(st.nextToken());
                        if(Float.isInfinite(z) || Float.isNaN(z) || z < Node.OBS)
                            z = Float.MIN_VALUE;
                        temp[i][j] = new Node(i, j, z);
                    }
                if(!FileManager.disableLoadIndicator)
                {
                    System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
                    System.out.print("DTM Readed: "+(j*100/rows)+"%");
                }
            }
            // Close file and return array
            zfile.close();
            return temp;
        
        }catch(FileNotFoundException nf){
            if(!FileManager.disableLoadIndicator)
            System.out.println("\nDTM file not found: " + file);
        }catch(IOException ioe){
            if(!FileManager.disableLoadIndicator)
            System.out.println("\nInput/output error: " + ioe.toString());
        }
        return null; // Fail to charge
    }
    
    /**
     * Read the transversal cost values file and store the information into a matrix.
     * @param file Relative path to the file to be read.
     * @return Null if there is any problem or the matrix cost otherwise.
    */
    public static float[][] ReadCfile(String file) {  
        String abspath = new File(file).getAbsolutePath();
        
        try{
            FileReader fr = new FileReader(abspath);
            BufferedReader cfile = new BufferedReader(fr);

            int cols, rows = 1;
            // Read first row to get number of cols
            StringTokenizer st = new StringTokenizer(cfile.readLine());
            cols = st.countTokens();
            while(cfile.readLine() != null)
                rows++;
            // Reset buffer reader and create the array
            fr = new FileReader(abspath);
            cfile = new BufferedReader(fr);
            float temp[][] = new float[cols][rows];

            // Read file
            if(!FileManager.disableLoadIndicator)
                System.out.println();
            for(int j = 0; j < rows; j++)
            {
                st = new StringTokenizer(cfile.readLine());
                for(int i = 0; i < cols; i++)
                {
                    temp[i][j] = Float.valueOf(st.nextToken());
                    if(temp[i][j] == 0)
                        temp[i][j] = 1; // Prevent 0 cost
                }
                if(!FileManager.disableLoadIndicator)
                {
                    System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
                    System.out.print("Costmap readed: "+(j*100/rows)+"%");
                }
            }
            if(!FileManager.disableLoadIndicator)
                System.out.println();
            // Close file and return array
            cfile.close();
            return temp;
        
        }catch(FileNotFoundException nf){
            if(!FileManager.disableLoadIndicator)
            System.out.println("\nCostmap file not found: " + file);
        }catch(IOException ioe){
            if(!FileManager.disableLoadIndicator)
            System.out.println("\nInput/output error: " + ioe.toString());
        }catch(Exception nf){
            if(!FileManager.disableLoadIndicator)
            System.out.println("\nException: " + nf.toString());
        }
        return null; // Fail to charge
    }
    
    /**
     * Read the transversal cost values file and store the information into a matrix, whose dimensions are known.
     * @param file Relative path to the file to be read.
     * @param cols number of columns (x values)
     * @param rows number of rows (y values)
     * @return Null if there is any problem or the matrix cost otherwise.
    */
    public static float[][] ReadCfile(String file, int cols, int rows) {  
        if(file == null || cols < 1 || rows < 1)
            return null;
        String abspath = new File(file).getAbsolutePath();
        
        try{
            FileReader fr = new FileReader(abspath);
            BufferedReader cfile = new BufferedReader(fr);
            float temp[][] = new float[cols][rows];
            // Read file
            if(!FileManager.disableLoadIndicator)
                System.out.println();
            for(int j = 0; j < rows; j++)
            {
                StringTokenizer st = new StringTokenizer(cfile.readLine());
                for(int i = 0; i < cols; i++)
                {
                    temp[i][j] = Float.valueOf(st.nextToken());
                    if(temp[i][j] == 0)
                        temp[i][j] = 1; // Prevent 0 cost
                }
                if(!FileManager.disableLoadIndicator)
                {
                    System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
                    System.out.print("Costmap readed: "+(j*100/rows)+"%");
                }
            }
            if(!FileManager.disableLoadIndicator)
                System.out.println();
            // Close file and return array
            cfile.close();
            return temp;
        
        }catch(FileNotFoundException nf){
            if(!FileManager.disableLoadIndicator)
            System.out.println("\nCostmap file not found: " + file);
        }catch(IOException ioe){
            if(!FileManager.disableLoadIndicator)
            System.out.println("\nInput/output error: " + ioe.toString());
        }catch(Exception nf){
            if(!FileManager.disableLoadIndicator)
            System.out.println("\nException: " + nf.toString());
        }
        return null; // Fail to charge
    }
    
    /**
     * Write a new file with random Z values in a matrix format readable with gnuplot.
     * It uses the Hill algorithm.
     * @param file Relative path to the new file.
     * @param cols Number of cols (X). Greater than 0.
     * @param rows Number of rows (Y). Greater than 0.
     * @param iter Maximum value of Z. Greater or equal to 0.
     * @return A bidimensional Node array with the DEM info.
     */
    public static Node[][] GenerateZfile(String file, int cols, int rows, int iter)
    {
        // Check cols, rows and iter greater than 0
        if(cols <= 0 || rows <= 0 || iter < 0)
        {
            System.out.println("Cols, rows and iterations must be greater than 0 [" + cols + "," + rows + "](" + iter + ")");
            return null;
        }
        Node temp[][] = new Node[cols][rows];
        String abspath = new File("").getAbsolutePath();
        
        try{
            FileWriter fw = new FileWriter(abspath + File.separator + file);
            PrintWriter zfile = new PrintWriter(fw);
 
            // Set initial z to 0
            for(int j = 0; j < rows; j++)
                for(int i = 0; i < cols; i++)
                    temp[i][j] = new Node(i, j, 0);
            
            // Iteration for terrain elevation
            int r, cx, cy;
            float max = -1;
            Random rand = new Random();
            for(int i = 0; i < iter; i++)
            {
                r = rand.nextInt(Math.min(cols, rows) / 5)+1;
                cx = rand.nextInt(cols);
                cy = rand.nextInt(rows);
                for(int x = 0; x < cols; x++)
                    for(int y = 0; y < rows; y++)
                    {
                        float nz = (float)(r*r-(Math.pow(x-cx,2)+Math.pow(y-cy,2)));
                        if(nz > 0)
                            temp[x][y].setZ(temp[x][y].getZ()+nz);
                        if(temp[x][y].getZ() > max)
                            max = temp[x][y].getZ();
                    }
            }
            // Normalize [0:cols/4]
            for(int x = 0; x < cols; x++)
                    for(int y = 0; y < rows; y++)
                        temp[x][y].setZ(temp[x][y].getZ()/(max/cols*4));
            
            // Write file
            for(int j = 0; j < rows; j++)
            {
                for(int i = 0; i < cols; i++)
                    zfile.print(" " + temp[i][j].getZ());
                // New row
		zfile.println(" ");
            }
            // Close file and return array
            zfile.close();        
            return temp;
            
        }catch(IOException ioe){
            System.out.println("Input/output error: " + ioe.toString());
            return null;
        }
    }
    
    /**
     * Write a new file with random C values and maximum obstacles % in a matrix format readable with gnuplot.
     * It is not guaranteed to converge if maxo is greater than 40.
     * @param file Relative path to the new file.
     * @param cols Number of cols (X). Greater than 0.
     * @param rows Number of rows (Y). Greater than 0.
     * @param maxo Maximum number of obstacles (%). Greater or equal to 0.
     * @return A bidimensional float array with the DEM info.
     */
    public static float[][] GenerateCfile(String file, int cols, int rows, int maxo)
    {
        // Check cols, rows and maxZ greater than 0
        if(cols <= 0 || rows <= 0 || maxo < 0)
        {
            System.out.println("Cols, rows and oblstacles % must be greater than 0 [" + cols + "," + rows + "](" + maxo + ")");
            return null;
        }
        float temp[][] = new float[cols][rows];
        String abspath = new File("").getAbsolutePath();
        
        try{
            FileWriter fw = new FileWriter(abspath + File.separator + file);
            PrintWriter cfile = new PrintWriter(fw);
 
            // Set initial cost to 1
            for(int j = 0; j < rows; j++)
                for(int i = 0; i < cols; i++)
                    temp[i][j] = 1;
            
            int ox, oy;
            int DXO = cols / 25; // Obstacle dimensions
            int DYO = rows / 25;
            int nblockcells = (maxo * cols * rows) / 100;
            int nobs;
            if(DXO*DYO < 1)
                nobs = nblockcells;
            else
                nobs = nblockcells / (DXO * DYO);
            Random rand = new Random();
            while(nblockcells > 0)
            {
                ox = rand.nextInt(rows);
                oy = rand.nextInt(cols);
                // Put obstacles
                for(int x = ox; x < ox+DXO; x++)
                    for(int y = oy; y < oy+DYO; y++)
			            // Protect first/last col/row
                        if(x >= 2+4 && x < cols-10 && y >= 2+4 && y < rows-10)
                            if(nblockcells > 0 && temp[x][y] == 1)
                            {
                                temp[x][y] = Map.MAX_COST+1;
                                nblockcells--;
                            }
                nobs--;
                // Put transversable area around obstacle
                for(int x = ox-1; x <= ox+DXO; x++)
                    for(int y = oy-1; y <= oy+DYO; y++)
                        if(x >= 0 && x < cols && y >= 0 && y < rows)
                        if(temp[x][y] == 1) temp[x][y] = 2;
            }
            
            for(int i = 0; i < (cols*rows)/(DXO*DYO); i++)
            {
                if(i>(cols*rows)/(DXO*DYO)/2)
                    nobs = rand.nextInt((int)Map.MAX_COST)+1;
                else
                    nobs = rand.nextInt((int)Map.MAX_COST/2)+1;
                ox = rand.nextInt(rows);
                oy = rand.nextInt(cols);
                // Put transversal area cost
                for(int x = ox; x < ox+DXO*4; x++)
                   for(int y = oy; y < oy+DYO*4; y++)
                   if(x < cols && y < rows && temp[x][y] < 3)
                       temp[x][y] = nobs;
            }
            
            // Write file
            for(int j = 0; j < rows; j++)
            {
                for(int i = 0; i < cols; i++)
                    cfile.print(" " + (int) temp[i][j]);
                // New row
		        cfile.println(" ");
            }          
            // Close file and return array
            cfile.close();        
            return temp;
            
        }catch(IOException ioe){
            System.out.println("Input/output error: " + ioe.toString());
            return null;
        }
    }
    
    /**
     * Write a new indoor cost file with random selected cells of NxN. Cells named from r0 to r5.
     * @param file Relative path to the new file.
     * @param cols Number of horizontal cells (X). Greater than 0.
     * @param rows Number of vertical cells (Y). Greater than 0.
     * @param cellsize size of the cell employed in generation (must be NxN),
     * @return A bidimensional float array with the indoor info.
     */
    public static int[][] GenerateRfile(String file, int cols, int rows, int cellsize)
    {
        // Check cols, rows and cellsize greater than 0
        if(cols <= 0 || rows <= 0 || cellsize < 0)
        {
            System.out.println("Cols, rows and cellsize must be greater than 0 [" + cols + "," + rows + "](" + cellsize + ")");
            return null;
        }
        int temp[][] = new int[cols*cellsize][rows*cellsize];
        String abspath = new File("").getAbsolutePath();
        Random rand = new Random();
        String rfile;
        
        try{
            for(int j = 0; j < rows; j++)
            for(int i = 0; i < cols; i++)
            {
                rfile = "r"+Integer.toString(rand.nextInt(6));  
                // Input cell
                FileReader fr = new FileReader(abspath + File.separator + rfile);
                BufferedReader brfile = new BufferedReader(fr);
                StringTokenizer st;
                // Read cell file
                for(int n = 0; n < cellsize; n++)
                {
                    st = new StringTokenizer(brfile.readLine());
                    for(int k = 0; k < cellsize; k++)
                        temp[k+i*cellsize][n+j*cellsize] = Integer.valueOf(st.nextToken());
                }
                brfile.close();
            }
            // Output file
            FileWriter fw = new FileWriter(abspath + File.separator + file);
            PrintWriter cfile = new PrintWriter(fw);
            for(int j = 0; j < rows*cellsize; j++)
            {
                for(int i = 0; i < cols*cellsize; i++)
                    cfile.print(" " + (int) temp[i][j]);
		        cfile.println(" ");
            }          
            // Close file and return array
            cfile.close();        
            return temp;
            
        }catch(FileNotFoundException nf){
            System.out.println("No input COST file found: " + file);
            return null;
        }catch(IOException ioe){
            System.out.println("Input/output error: " + ioe.toString());
            return null;
        }
    }
    
   /**
    * Write a path file in a gnuplot format. This function accepts two types of lists:
    * ArrayList<Node> or ArrayList<double[][3]>.
    * @param file Relative path to the new file.
    * @param path A list of nodes that represent a valid path.
    * @param headerdata Data to print at top of the file.
    */
   public static void WritePathFile(String file, ArrayList path, String headerdata)
   {
       String abspath = new File("").getAbsolutePath();

       try{
           FileWriter fw = new FileWriter(abspath + File.separator + file);
           PrintWriter pfile = new PrintWriter(fw);
           // Print number of steps and total cost
           pfile.println(headerdata);
           if(path != null)
           {
                // Read the array and write into file
                for(int i = 0; i < path.size(); i++)
                    if(path.get(i).getClass() == Node.class) // Check type of list
                        pfile.println(((Node)path.get(i)).getXYZ());
                    else
                        pfile.println(((double[])path.get(i))[0]+" "+((double[])path.get(i))[1]+" "+((double[])path.get(i))[2]);
           }
           else
               pfile.println("NO PATH FOUND!");
           pfile.close();                   

       }catch(IOException ioe){
           System.out.println("Input/output error: " + ioe.toString());
       }
   }

   public static ArrayList ReadPathfile(String file) 
   {
        String abspath = new File(file).getAbsolutePath();
                
        try{
            FileReader fr = new FileReader(abspath);
            BufferedReader zfile = new BufferedReader(fr);

            ArrayList<double[]> temp = new ArrayList();
            StringTokenizer st;
            String tok;
            String line = zfile.readLine();
            while(line != null)
            {
                st = new StringTokenizer(line);
                if(st.countTokens() > 1)
                {
                    tok = st.nextToken();
                    if(Character.isDigit(tok.charAt(0)))
                    {
                        double[] point = new double[3]; point[2] = 0;
                        point[0] = Double.valueOf(tok);
                        point[1] = Double.valueOf(st.nextToken());
                        if(st.hasMoreTokens())
                            point[2] = Double.valueOf(st.nextToken());
                        temp.add(point);
                    }
                    else if(tok.equals("MOVE_TO") || tok.equals("GO_TO") || tok.equals("MOVETO") || tok.equals("GOTO"))
                    {
                        double[] point = new double[3]; point[2] = 0;
                        point[0] = Double.valueOf(st.nextToken());
                        point[1] = Double.valueOf(st.nextToken());
                        if(st.hasMoreTokens())
                            point[2] = Double.valueOf(st.nextToken());
                        temp.add(point);
                    }
                }
                line = zfile.readLine();
            }
            
            // Close file and return array
            zfile.close();
            return temp;
        
        }catch(FileNotFoundException nf){
            if(!FileManager.disableLoadIndicator)
            System.out.println("Path file not found: " + file);
        }catch(IOException ioe){
            if(!FileManager.disableLoadIndicator)
            System.out.println("Input/output error: " + ioe.toString());
        }
        return null; // Fail to charge
    }
   
    /**
     * Create a new text file with the desired content.
     * @param filename filename for the output
     * @param content text to write into the file
     * @return true if the content is written, false otherwise
     */
    public static boolean writeTextFile(String filename, String content)
    {
        try{
            PrintWriter output = new PrintWriter(filename);
            output.print(content);
            output.close();
            return true;
        }catch(java.io.FileNotFoundException fnf) {
            System.out.println("Problem writing file "+filename);
            return false;
        }
    }
   
}
