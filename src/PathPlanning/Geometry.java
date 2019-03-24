
package PathPlanning;

/**
 * Geometry related functions.
 * @author Pablo muñoz
 */
public abstract class Geometry {
     
    /** Conversion between radians and degrees. */
    public final static float CONV = (float)(180/Math.PI);

    /**
     * Calculate the hypotenuse of a triangle using the other two sides.
     * @param dx side 1.
     * @param dy side 2.
     * @return The hypotenuse of the triangle.
     */
    public static float LongHyp(float dx, float dy)
    {
        return (float)Math.sqrt(dx*dx + dy*dy);
    }
    
    /**
     * Calculate the hypotenuse of a triangle using the other two sides and a third dimension.
     * @param dx side 1.
     * @param dy side 2.
     * @param dz third dimension.
     * @return The hypotenuse of the triangle.
     */
    public static float LongHypZ(float dx, float dy, float dz)
    {
	return (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    /**
     * Calculate the hypotenuse of a triangle using the cosine theorem.
     * @param dx one side of the triangle.
     * @param dy another side.
     * @param ang opposite angle of the hypotenuse (in degrees).
     * @return The longitude of the hypotenuse.
     */
    public static float LongHypA(float dx, float dy, float ang)
    {
        return (float)Math.sqrt(dx*dx + dy*dy - 2*dx*dy*Math.cos(ang/CONV));
    }

    /**
     * Calculate one angle of a triangle using the cosine theorem. <b>Both dx and dy
     * must be greater than 0.</b>
     * @param dx one side.
     * @param dy another side.
     * @param de hypotenuse of the triangle. Opposite angle is returned.
     * @return The opposite angle (in degrees) of the <b>hypotenuse</b>.
     */
    public static float AngleHyp(float dx, float dy, float de)
    {
        return (float)(Math.acos((dx*dx + dy*dy - de*de) / (2*dx*dy)) * CONV);
    }

    /**
     * Calculates the angle for a x,y node pointing from the 0,0 (origin of axis).
     * @param x x position.
     * @param y y position.
     * @return The angle in degrees from 0,0 to (x,y) point.
     */
    public static float Angle(float x, float y)
    {
        return (float)(Math.atan2(x, y) * CONV); 
    }
      
    /**
     * Calculate one angle of a triangle using the cosine theorem. <i>dx or dy can be 0.
     * Both can be positive or negative in order to obtain the correct angle.</i>
     * <pre>
     *	     Q3 | Q4		dx
     *	      --+--> 'X'	---
     *	     Q2 | Q1		\ | dy
     *		v 'Y'	      de \|
     * </pre>
     * @param dx one side.
     * @param dy another side. Opposite angle is returned.
     * @param de hypotenuse of the triangle.
     * @return The opposite angle (in degrees) of the <b>dy</b> segment. 
     */
    public static float Angle(float dx, float dy, float de)
    {
        if(dx != 0) // Avoid NaN
            return OffsetAngle((float)Math.acos((dx*dx + de*de - dy*dy) / Math.abs(2*dx*de)) * CONV, dx, dy);
        else
            if(dy != 0)
            {
                if(dy > 0)
                    return 90;
                else
                    return 270;
            }
            else // dx==0  && dy==0
		return 0;
    }
    
    /**
     * Set the correct angle value in [0º,360º).
     * @param angle angle to correct.
     * @param quadrant quadrant in which angle is.
     * @return The corrected angle value between [0º,360º).
     */
    public static float OffsetAngle(float angle, int quadrant)
    {
        if(quadrant == 1) return angle;
        if(quadrant == 2) return 180 - angle;
        if(quadrant == 3) return 180 + angle;
        if(quadrant == 4) return 360 - angle;
        return 0;
    }
    
    /**
     * Set the correct angle value in [0º,360º).
     * @param angle angle to correct.
     * @param x x coord of the triangle vertex. 
     * @param y y coord of the triangle vertex.
     * @return The corrected angle value between [0º,360º).
     */
    public static float OffsetAngle(float angle, float x, float y)
    {
        if(x >= 0 && y >= 0) return angle;
        if(x < 0  && y >= 0) return 180 - angle;
        if(x < 0  && y < 0 ) return 180 + angle;
        if(x >= 0 && y < 0 ) return 360 - angle;
        return 0;
    }
    
    /**
     * Allows to know the quadrant that owns the given node.
     * @param x x coordinate of the point.
     * @param y y coordinate of the point.
     * @return The quadrant number (from 1 to 4) in which the x,y point is located.
     */
    public static int Quadrant(float x, float y)
    {
        if(x >= 0 && y >= 0) return 1;
        if(x < 0  && y >= 0) return 2;
        if(x < 0  && y < 0 ) return 3;
        if(x >= 0 && y < 0 ) return 4;
        return 0;
    }

    
    /**
     * Test if is line of sight from node pos to node succ (that is, no obstacles between them) for corner-node.
     * This is the standard Bresenham line-drawing algorithm from computer graphics (Bresenham, 1965).
     * @param map dem information.
     * @param pos actual position.
     * @param succ desired node to check line of sight.
     * @return true if there is line of sight from pos to succ. False otherwise.
     */
    public static boolean LineOfSightB(Map map, Node pos, Node succ)
    {
        int x0 = pos.getX();
        int y0 = pos.getY();
        int x1 = succ.getX();
        int y1 = succ.getY();
        int dy = y1 - y0;
        int dx = x1 - x0;
        int f = 0;
        int sx, sy;
        if(dy < 0)
        {
            dy = -dy;
            sy = -1;
        }
        else
            sy = 1;
        if(dx < 0)
        {
            dx = -dx;
            sx = -1;
        }
        else
            sx = 1;
        if(dx >= dy)
            while(x0 != x1)
            {
                f += dy;
                if(f >= dx)
                {
                    if(map.get_tcost(x0+((sx-1)/2), y0+((sy-1)/2)) > Map.MAX_COST)
                        return false;
                    y0 += sy;
                    f -= dx;
                }
                if(f != 0 && (map.get_tcost(x0+((sx-1)/2), y0+((sy-1)/2)) > Map.MAX_COST))
                    return false;
                if(dy == 0 && (map.get_tcost(x0+((sx-1)/2), y0) > Map.MAX_COST) && (map.get_tcost(x0+((sx-1)/2), y0-1) > Map.MAX_COST))
                    return false;
                x0 += sx;
            }
        else
            while(y0 != y1)
            {
                f += dx;
                if(f >= dy)
                {
                    if(map.get_tcost(x0+((sx-1)/2), y0+((sy-1)/2)) > Map.MAX_COST)
                        return false;
                    x0 += sx;
                    f -= dy;
                }
                if(f != 0 && (map.get_tcost(x0+((sx-1)/2), y0+((sy-1)/2)) > Map.MAX_COST))
                    return false;
                if(dx == 0 && (map.get_tcost(x0, y0+((sy-1)/2)) > Map.MAX_COST) && (map.get_tcost(x0-1, y0+((sy-1)/2)) > Map.MAX_COST))
                    return false;
                y0 += sy;
            }
        return true;
    }
    
}
