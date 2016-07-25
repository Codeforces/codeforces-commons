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
        assertNotNull("pointA is null.", pointA);
        assertEquals("Wrong pointA.x.", 0.0D, pointA.getX(), Line2D.DEFAULT_EPSILON);
        assertEquals("Wrong pointA.y.", 0.0D, pointA.getY(), Line2D.DEFAULT_EPSILON);

        Point2D pointB = Line2D.getLineByTwoPoints(2.0D, 1.0D, 3.0D, 5000000.0D)
                .getIntersectionPoint(Line2D.getLineByTwoPoints(3.0D, 0.0D, 3.0D, -1.0D));
        assertNotNull("pointB is null.", pointB);
        assertEquals("Wrong pointB.x.", 3.0D, pointB.getX(), Line2D.DEFAULT_EPSILON);
        assertEquals("Wrong pointB.y.", 5000000.0D, pointB.getY(), Line2D.DEFAULT_EPSILON);

        Point2D pointC = Line2D.getLineByTwoPoints(-1000.0D, 50000000.0D, -1000.0D, -50000000.0D)
                .getIntersectionPoint(Line2D.getLineByTwoPoints(1.0D, 1.0D, 2.0D, 1.0D));
        assertNotNull("pointC is null.", pointC);
        assertEquals("Wrong pointC.x.", -1000.0D, pointC.getX(), Line2D.DEFAULT_EPSILON);
        assertEquals("Wrong pointC.y.", 1.0D, pointC.getY(), Line2D.DEFAULT_EPSILON);

        Point2D pointD = Line2D.getLineByTwoPoints(1.0D, 1.0D, 2.0D, 2.0D)
                .getIntersectionPoint(Line2D.getLineByTwoPoints(2.0D, 3.0D, -2.0D, -1.0D));
        assertNull("pointD is not null.", pointD);

        Point2D pointE = Line2D.getLineByTwoPoints(-900.0D, -800.0D, -800.0D, -600.0D)
                .getIntersectionPoint(Line2D.getLineByTwoPoints(-500.0D, -1500.0D, 0.0D, -2000.0D));
        assertNotNull("pointE is null.", pointE);
        assertEquals("Wrong pointE.x.", -1000.0D, pointE.getX(), Line2D.DEFAULT_EPSILON);
        assertEquals("Wrong pointE.y.", -1000.0D, pointE.getY(), Line2D.DEFAULT_EPSILON);
    }

    @SuppressWarnings("OverlyLongMethod")
    public void testGetIntersectionPointsWithCircle() throws Exception {
        Point2D[] pointsA = Line2D.getLineByTwoPoints(0.0D, 0.0D, 1000.0D, 0.0D)
                .getIntersectionPoints(new Circle2D(new Point2D(2000.0D, 0.0D), 1000.0D));
        assertNotNull("pointsA is null.", pointsA);
        assertEquals("Wrong pointsA.length.", 2, pointsA.length);
        assertTrue("Wrong pointsA[0].", new Point2D(1000.0D, 0.0D).nearlyEquals(pointsA[0]));
        assertTrue("Wrong pointsA[1].", new Point2D(3000.0D, 0.0D).nearlyEquals(pointsA[1]));

        Point2D[] pointsB = Line2D.getLineByTwoPoints(0.0D, 0.0D, -1000.0D, -1000.0D)
                .getIntersectionPoints(new Circle2D(new Point2D(100.0D, 0.0D), 100.0D));
        assertNotNull("pointsB is null.", pointsB);
        assertEquals("Wrong pointsB.length.", 2, pointsB.length);
        assertTrue("Wrong pointsB[0].", new Point2D(0.0D, 0.0D).nearlyEquals(pointsB[0]));
        assertTrue("Wrong pointsB[1].", new Point2D(100.0D, 100.0D).nearlyEquals(pointsB[1]));

        Point2D[] pointsC = Line2D.getLineByTwoPoints(0.0D, 0.0D, 1000.0D, 1000.0D)
                .getIntersectionPoints(new Circle2D(new Point2D(200.0D, 100.0D), 100.0D));
        assertNotNull("pointsC is null.", pointsC);
        assertEquals("Wrong pointsC.length.", 2, pointsC.length);
        assertTrue("Wrong pointsC[0].", new Point2D(100.0D, 100.0D).nearlyEquals(pointsC[0]));
        assertTrue("Wrong pointsC[1].", new Point2D(200.0D, 200.0D).nearlyEquals(pointsC[1]));

        Point2D[] pointsD = Line2D.getLineByTwoPoints(0.0D, 0.0D, 0.0D, 1000.0D)
                .getIntersectionPoints(new Circle2D(new Point2D(100.001D, 0.0D), 100.0D));
        assertNotNull("pointsD is null.", pointsD);
        assertEquals("Wrong pointsD.length.", 0, pointsD.length);

        Point2D[] pointsE = Line2D.getLineByTwoPoints(0.0D, 0.0D, 0.0D, -1000.0D)
                .getIntersectionPoints(new Circle2D(new Point2D(100.0000000001D, 0.0D), 100.0D));
        assertNotNull("pointsE is null.", pointsE);
        assertEquals("Wrong pointsE.length.", 0, pointsE.length);

        Point2D[] pointsF = Line2D.getLineByTwoPoints(0.0D, 0.0D, 0.0D, -1000.0D)
                .getIntersectionPoints(new Circle2D(new Point2D(100.0D, 10000.0D), 100.0D));
        assertNotNull("pointsF is null.", pointsF);
        assertEquals("Wrong pointsF.length.", 1, pointsF.length);
        assertTrue("Wrong pointsF[0].", new Point2D(0.0D, 10000.0D).nearlyEquals(pointsF[0]));

        Point2D[] pointsG = Line2D.getLineByTwoPoints(-10000.0D, -10000.0D, 10000.0D, 10000.0D)
                .getIntersectionPoints(new Circle2D(new Point2D(0.0D, 100.0D), 100.0D));
        assertNotNull("pointsG is null.", pointsG);
        assertEquals("Wrong pointsG.length.", 2, pointsG.length);
        assertTrue("Wrong pointsG[0].", new Point2D(0.0D, 0.0D).nearlyEquals(pointsG[0]));
        assertTrue("Wrong pointsG[1].", new Point2D(100.0D, 100.0D).nearlyEquals(pointsG[1]));

        Point2D[] pointsH = Line2D.getLineByTwoPoints(0.0D, 1000.0D, 1000.0D, 0.0D)
                .getIntersectionPoints(new Circle2D(new Point2D(0.0D, 0.0D), 1000.0D));
        assertNotNull("pointsH is null.", pointsH);
        assertEquals("Wrong pointsH.length.", 2, pointsH.length);
        assertTrue("Wrong pointsH[0].", new Point2D(0.0D, 1000.0D).nearlyEquals(pointsH[0]));
        assertTrue("Wrong pointsH[1].", new Point2D(1000.0D, 0.0D).nearlyEquals(pointsH[1]));

        Point2D[] pointsI = Line2D.getLineByTwoPoints(0.0D, 3670.7106781186544D, 4000.0D, -329.28932188134536D)
                .getIntersectionPoints(new Circle2D(new Point2D(400.0D, 3600.0D), 971.9472503716837D));
        assertNotNull("pointsI is null.", pointsI);
        assertEquals("Wrong pointsI.length.", 2, pointsI.length);
        assertTrue("Wrong pointsI[0].", new Point2D(-431.9023805485233D, 4102.613058667178D).nearlyEquals(pointsI[0]));
        assertTrue("Wrong pointsI[1].", new Point2D(902.613058667178D, 2768.0976194514765D).nearlyEquals(pointsI[1]));
    }
}
