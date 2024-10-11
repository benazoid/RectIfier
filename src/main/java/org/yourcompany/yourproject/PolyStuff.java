package org.yourcompany.yourproject;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;

public class PolyStuff {

    public static Polygon polygonUnion(Polygon poly1, Polygon poly2){

        // Intersection of two convex polygons is a convex polygon

        // Edge intersections between two polygons are vertices in the intersection shape

        ArrayList<Point> ptList = new ArrayList<>();

        for(int i = 0; i < poly1.npoints; i++){
            Point pt1A = new Point(poly1.xpoints[i], poly1.ypoints[i]);
            Point pt1B = new Point(poly1.xpoints[(i+1)%poly1.npoints], poly1.ypoints[(i+1)%poly1.npoints]);
            for (int j = 0; j < poly2.npoints; j++) {
                Point pt2A = new Point(poly2.xpoints[j], poly2.ypoints[j]);
                Point pt2B = new Point(poly2.xpoints[(j+1)%poly2.npoints], poly2.ypoints[(j+1)%poly2.npoints]);

                Point intersection = lineIntersect(pt1A.x, pt1A.y, pt1B.x, pt1B.y, pt2A.x, pt2A.y, pt2B.x, pt2B.y);
                if (intersection instanceof Point) {
                    ptList.add(intersection);
                }
            }
        }

        // A vertex from a polygon that is contained in the other polygon is a vertex of the intersection shape

        for (int i = 0; i < poly1.npoints; i++) {
            Point pt = new Point(poly1.xpoints[i], poly1.ypoints[i]);
            if (poly2.contains(pt)) {
                ptList.add(pt);
            }
        }

        for (int i = 0; i < poly2.npoints; i++) {
            Point pt = new Point(poly2.xpoints[i], poly2.ypoints[i]);
            if (poly1.contains(pt)) {
                ptList.add(pt);
            }
        }

        if (ptList.isEmpty()) {
            System.err.println("polygons don't overlap");
        }

        // Find the point order so it's convex

        Point ave = new Point(0,0);
        for (Point pt : ptList) {
            ave.setLocation(ave.x + pt.x, ave.y + pt.y);
        }
        ave.setLocation(ave.x/ptList.size(), ave.y/ptList.size());

        ptList.sort((a,b)-> comparePts(a, b, ave));

        Polygon resultPoly = new Polygon();
        for (Point pt : ptList) {
            resultPoly.addPoint(pt.x, pt.y);
            //System.out.println(Integer.toString(pt.x) + ", " + Integer.toString(pt.y));
        }

        return resultPoly;
    }

    public static int comparePts(Point a, Point b, Point ave){
        double angleA = Math.atan2(a.y-ave.y, a.x-ave.x);
        double angleB = Math.atan2(b.y-ave.y, b.x-ave.x);
        return (int) Math.signum(angleB-angleA);
    }

    public static Point lineIntersect(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0) { // Lines are parallel.
            return null;
        }
        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))/denom;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3))/denom;
        if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
            // Get the intersection point.
            Point pt = new Point((int) (x1 + ua*(x2 - x1)), (int) (y1 + ua*(y2 - y1)));
            //System.out.println(Integer.toString(pt.x) + " : " + Integer.toString(pt.y));
            return pt;
        }
    
        return null;
    }

    public static double getArea(Polygon poly){
        // Get center point
        Point ave = new Point(0,0);
        for (int i = 0; i < poly.npoints; i++) {
            ave.x += poly.xpoints[i];
            ave.y += poly.ypoints[i];
        }
        ave.x /= poly.npoints;
        ave.y /= poly.npoints;

        double totalArea = 0;
        
        for (int i = 0; i < poly.npoints; i++) {
            Point p1 = new Point(poly.xpoints[i], poly.ypoints[i]);
            Point p2 = new Point(poly.xpoints[(i+1)%poly.npoints], poly.ypoints[(i+1)%poly.npoints]);
            totalArea += Math.abs((ave.x*(p1.y-p2.y) + p1.x*(p2.y-ave.y) + p2.x*(ave.y-p1.y))/2.0);
        }

        return totalArea;
    }

    public static void log(Polygon poly){
        System.out.println("Poly log");
        for (int i = 0; i < poly.npoints; i++) {
            System.out.println(Integer.toString(poly.xpoints[i]) + ", " + Integer.toString(poly.ypoints[i]));
        }
    }

    // Returns list representing segments, x is the top pos
    // y is bottom pos, y's sign is the slope

    // top, bottom, direction
    public static class TBD{
        public int top;
        public int bottom;
        public boolean direction;
        
        public TBD(int top_, int bottom_, boolean direction_){
            top = top_;
            bottom = bottom_;
            direction = direction_;
        }
    }
    public static TBD[] getTopBottomDirection(Polygon poly){

        TBD[] tbds = new TBD[poly.npoints];

        for (int i = 0; i < poly.npoints; i++) {

            // if slope is -, the line goes down going counter clockwise vise versa, if 0, horizontal
            int yPtA = poly.ypoints[i];
            int yPtB = poly.ypoints[(i+1)%poly.npoints];
            int slope = yPtA - yPtB;

            tbds[i] = new TBD(Math.min(yPtA, yPtB), Math.max(yPtA, yPtB), slope < 0);
        }
        return tbds;
    }

    // Returns point, x is start pt, y is end pt
    public static Point getStartAndEnd(Polygon poly, int yPos, TBD[] topsAndBottoms){

        Point resultPt = new Point();

        for (int i = 0; i < topsAndBottoms.length; i++) {
            int top = topsAndBottoms[i].top;
            int bottom = topsAndBottoms[i].bottom;
            boolean direction = topsAndBottoms[i].direction;
            
            if (top <= yPos && bottom > yPos && top != bottom) {

                int xDiff = poly.xpoints[i]-poly.xpoints[(i+1)%poly.npoints];
                int yDiff = poly.ypoints[i]-poly.ypoints[(i+1)%poly.npoints];

                if (yDiff == 0) {
                    System.out.println(Integer.toString(top) + " : " + Integer.toString(bottom));
                }

                double lineSlope = xDiff/yDiff;

                Point linePt; 
                if (poly.ypoints[i] < poly.ypoints[(i+1)%poly.npoints]) {
                    linePt = new Point(poly.xpoints[i], poly.ypoints[i]);
                } else {
                    linePt = new Point(poly.xpoints[(i+1)%poly.npoints], poly.ypoints[(i+1)%poly.npoints]);
                }

                // x = m(y-y0)+x0

                int xPos = (int)(lineSlope * (yPos-linePt.y) + linePt.x);

                if(!direction){
                    resultPt.y = xPos;
                } else{
                    resultPt.x = xPos;
                }
            }
        }
        return resultPt;
    }

    public static Point getTopAndBottom(Polygon poly){
        int bestTop = 1000000000;
        int bestBottom = -1000000000;
        for (int i = 0; i < poly.npoints; i++) {
            if (poly.ypoints[i] < bestTop) {
                bestTop = poly.ypoints[i];
            }
            if (poly.ypoints[i] > bestBottom) {
                bestBottom = poly.ypoints[i];
            }
        }
        return new Point(bestTop, bestBottom);
    }

}
