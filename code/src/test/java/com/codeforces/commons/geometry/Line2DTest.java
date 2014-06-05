package com.codeforces.commons.geometry;

import junit.framework.TestCase;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 05.06.14
 */
public class Line2DTest extends TestCase {
    public void testGetIntersectionPoint() throws Exception {
        Point2D pointA = Line2D.getLineByTwoPoints(0.0D, 0.0D, 1.0D, 1.0D)
                .getIntersectionPoint(Line2D.getLineByTwoPoints(0.0D, 0.0D, -1.0D, 1.0D));
        assertEquals("Wrong pointA.x.", 0.0D, pointA.getX(), Line2D.DEFAULT_EPSILON);
        assertEquals("Wrong pointA.y.", 0.0D, pointA.getY(), Line2D.DEFAULT_EPSILON);

        Point2D pointB = Line2D.getLineByTwoPoints(2.0D, 1.0D, 3.0D, 5000000.0D)
                .getIntersectionPoint(Line2D.getLineByTwoPoints(3.0D, 0.0D, 3.0D, -1.0D));
        assertEquals("Wrong pointB.x.", 3.0D, pointB.getX(), Line2D.DEFAULT_EPSILON);
        assertEquals("Wrong pointB.y.", 5000000.0D, pointB.getY(), Line2D.DEFAULT_EPSILON);

        Point2D pointC = Line2D.getLineByTwoPoints(-1000.0D, 50000000.0D, -1000.0D, -50000000.0D)
                .getIntersectionPoint(Line2D.getLineByTwoPoints(1.0D, 1.0D, 2.0D, 1.0D));
        assertEquals("Wrong pointC.x.", -1000.0D, pointC.getX(), Line2D.DEFAULT_EPSILON);
        assertEquals("Wrong pointC.y.", 1.0D, pointC.getY(), Line2D.DEFAULT_EPSILON);

        Point2D pointD = Line2D.getLineByTwoPoints(1.0D, 1.0D, 2.0D, 2.0D)
                .getIntersectionPoint(Line2D.getLineByTwoPoints(2.0D, 3.0D, -2.0D, -1.0D));
        assertNull("pointD is not null.", pointD);

        Point2D pointE = Line2D.getLineByTwoPoints(-900.0D, -800.0D, -800.0D, -600.0D)
                .getIntersectionPoint(Line2D.getLineByTwoPoints(-500.0D, -1500.0D, 0.0D, -2000.0D));
        assertEquals("Wrong pointE.x.", -1000.0D, pointE.getX(), Line2D.DEFAULT_EPSILON);
        assertEquals("Wrong pointE.y.", -1000.0D, pointE.getY(), Line2D.DEFAULT_EPSILON);
    }
}
