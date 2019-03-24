
package PathPlanning;

/**
 * Implementation of a node element with data and cost and heuristics information.
 * @author Pablo Muñoz
 */
public class Node implements Comparable {
 
    /** Define the min Z value. Values less than OBS represents and obstacle. */
    public static final float OBS = -10000;
    /** X position. */
    private int X;
    /** Y position. */
    private int Y;
    /** Z position. */
    private float Z;
    /** Terrain property. */
    public float A;
    /** Indicate that the node has been expanded. */
    private boolean expanded;
    /** Value of evaluation function. */
    private float F;
    /** Value of cost to reach the node from the actual node. */
    private float G;
    /** Value of heuristics evaluation. */
    private float H;
    /** Parent of the node. */
    private Node parent;
    /** Specify how to make tie breaking between nodes with same F value.
     * True for first node with less H value. False for first node with high H 
     * value (that is, less G value). Initially set to true.
     */
    private static boolean LESS_H_FIRST = true;
    
    /** 
     * Set the (x,y,z) coordinates of the node. If Z < 0, then is an obstacle.
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param z Z coordinate.
     */
    public Node(int x, int y, float z)
    {
        X = x;
        Y = y;
        Z = z;
        F = Float.MAX_VALUE;
        G = Float.MAX_VALUE;
        H = 0;
        A = -1;
        expanded = false;
        parent = null;
    }
    
    /**
     * Copy constructor for node.
     * @param n node to copy.
     */
    public Node(final Node n)
    {
        X = n.getX(); Y = n.getY(); Z = n.getZ();
        F = n.getF(); G = n.getG(); H = n.getH();
        A = n.A;
        expanded = n.isExpanded();
        parent = n.getParent();
    }
    
    /**
     * Change the tie break flag for all nodes. Initially set to true.
     */
    public static void ChangeTieBreakingFlag()
    {
        LESS_H_FIRST = !LESS_H_FIRST;
    }

    /**
     * Comparison between nodes. 
     * @param no node to compare.
     * @return -1 if the node is less than no or 1 otherwise. Return 0 for equals F. 
     * The comparison takes into account the F values, and, if both nodes have 
     * the same F, compares the H values returning -1 depending of the tie breaking flag.
     */
    @Override
    public int compareTo(Object no)
    {
        final int BEFORE =-1;
        final int EQUAL  = 0;
        final int AFTER  = 1;
        Node n = (Node)no;
        if(n.getF() < F) return AFTER;
        if(n.getF() > F) return BEFORE;
        if(n.getH() == H) return EQUAL;
        if(LESS_H_FIRST)
        {
            if(n.getH() < H) return AFTER;
            else return BEFORE;
        }
        else
        {
            if(n.getH() > H) return AFTER;
            else return BEFORE;
        }
    }
    
    /**
     * Compare a node object based on their coordinates.
     * @param no object to compare.
     * @return true only and only if the coordinates of the two objects are the same.
     */
    @Override
    public boolean equals(Object no)
    {
        Node n = (Node)no;
        return no != null && X == n.getX() && Y == n.getY();// && Z == n.getZ();
    }

    /**
     * Formating the data of the node as (X Y Z)[G+H] F.
     * @return A string with the relevant data of the node.
     */
    @Override
    public String toString()
    {
	java.text.DecimalFormat df = new java.text.DecimalFormat("0.00000");
        return "(" + getXYZ() + ")[" + df.format(G) + "+" + df.format(H) + "]  " + df.format(F);
    }
    
    /**
     * @return An array with the node coordinates [X, Y, Z].
     */
    public double[] toArray()
    {
        return new double[]{X, Y, Z};
    }
    
    /**
     * @return A string like "X Y Z".
     */
    public String getXYZ()
    {
        return String.valueOf(X) + " " + String.valueOf(Y) + " " + String.valueOf(Z);
    }
    
    /**
     * Change the Z value. Useful to put a previously unknown obstacle.
     * @param z new Z value.
     */
    public void setZ(float z)
    {
        Z = z;
    }
    
    /**
     * Mark the node as an obstacle.
     */
    public void setObstacle()
    {
        Z = OBS - 1;
    }
    
    /**
     * Test if the point is an obstacle.
     * @return true if the point is an obstacle, false otherwise.
     */
    public boolean isObstacle()
    {
        return Z < OBS || Z == Float.MIN_VALUE || Z == Float.MAX_VALUE;
    }
    
    /**
     * @return true if the node has been expanded
     */
    public boolean isExpanded()
    {
        return expanded;
    }
    
    /**
     * Set the expansion property of the node
     * @param exp the expanded value
     */
    public void setExpanded(boolean exp)
    {
        expanded = exp;
    }
    
    /**
     * Set a new parent for the node.
     * @param p new parent.
     */
    public void setParent(Node p)
    {
        if(p != null)
            expanded = true;
        parent = p;
    }
    
    /**
     * Set the value of the F function. If value is less than 0, F is set to 1.
     * @param value F value, greater or equal to 0.
     */
    public void setF(float value)
    {
        if(value < 0)
            F = 1;
        else
            F = value;
    }
    
    /**
     * Set value of G and H, and F = G + H. If any value is less than 0, the value is set to 0.
     * @param gval G value (greater or equal to 0).
     * @param hval H value (greater or equal to 0).
     */
    public void setF(float gval, float hval)
    {
        setG(gval);
        setH(hval);
        F = G + H;
    }
    
    /**
     * Set the value of G (cost) function. If value is less than 0, G is set to 1.
     * @param value G value, greater or equal to 0.
     */
    public void setG(float value)
    {
        if(value < 0)
            G = 1;
        else
            G = value;
    }
    
    /**
     * Set the value of H (heuristics) function. If value is less than 0, H is set to 1.
     * @param value H value, greater or equal to 0.
     */
    public void setH(float value)
    {
        if(value < 0)
            H = 1;
        else
            H = value;
    }
    
    /**
     * @return The X value of the node.
     */
    public int getX()
    {
        return X;
    }
    
    /**
     * @return The Y value of the node.
     */
    public int getY()
    {
        return Y;
    }
    
    /**
     * @return The Z value of the node.
     */
    public float getZ()
    {
        return Z;
    }
    
    /**
     * @return The F (G+H) value of the node.
     */
    public float getF()
    {
        return F;
    }
    
    /**
     * @return The G (cost) value of the node.
     */
    public float getG()
    {
        return G;
    }
    
    /**
     * @return The H (heuristics) value of the node.
     */
    public float getH()
    {
        return H;
    }
    
    /**
     * @return The parent of the node.
     */
    public Node getParent()
    {
        return parent;
    }
    
}
