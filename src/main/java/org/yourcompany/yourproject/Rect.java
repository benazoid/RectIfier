package org.yourcompany.yourproject;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

public final class Rect {

    static int screenWidth;
    static int screenHeight;

    final static int SIZE_MAX = 100;
    final static int SIZE_MIN = 5;
    
    final int xPos;
    final int yPos;
    final int width;
    final int height;
    final double rotation;
    final Color color;

    final BufferedImage targetImg;

    private Polygon intersectionPoly;
    private boolean insideCanvas = true;

    private double score = Math.PI; // pi here is very arbitrary

    public Rect(int x, int y, int w, int h, double rot, BufferedImage targetImg_){
        xPos = x;
        yPos = y;
        width = w;
        height = h;
        rotation = rot;

        targetImg = targetImg_;

        intersectionPoly = getCanvasIntersection();

        color = getAverageColor(targetImg);
    }

    public double getScore(BufferedImage targetImage, double[][] baseScoreArr){
        if (score != Math.PI) {
            return score;
        }

        Point topAndBottom = PolyStuff.getTopAndBottom(intersectionPoly);
        PolyStuff.TBD[] lineTopsAndBottoms = PolyStuff.getTopBottomDirection(intersectionPoly);

        double rectScore = 0;
        double baseScore = 0;

        for(int y = topAndBottom.x; y < topAndBottom.y; y++){
            Point startAndEnd = PolyStuff.getStartAndEnd(intersectionPoly, y, lineTopsAndBottoms);
            for (int x = startAndEnd.x; x < startAndEnd.y; x++) {
                rectScore += MyCanvas.colorDifRGB(color.getRGB(), targetImage.getRGB(x, y));
                baseScore += baseScoreArr[x][y];
            }
        }
        score = (baseScore - rectScore);

        return score;
    }

    public int compareTo(Rect other, double[][] baseScoreArr){
        //double diff = -(mainCanvas.scoreLocal(this)-mainCanvas.scoreLocal(other));
        double diff = -(this.getScore(targetImg, baseScoreArr)-other.getScore(targetImg, baseScoreArr));
        return (int) Math.signum(diff);
    }

    // mutationAmt is the % of the full range available to mutate to
    public Rect makeMutatedChild(double mutationAmt){
        int x = (int) getRandomInRange(xPos, 0, screenWidth, mutationAmt);
        int y = (int) getRandomInRange(yPos, 0, screenHeight, mutationAmt);

        int w = (int) getRandomInRange(width, SIZE_MIN, SIZE_MAX, mutationAmt);
        int h = (int) getRandomInRange(width, SIZE_MIN, SIZE_MAX, mutationAmt);

        double rot  = getRandomInRange(rotation, 0, 2*Math.PI, mutationAmt);
        
        return new Rect(x, y, w, h, rot, targetImg);
    }

    public Polygon getCanvasIntersection(){

        double cos = Math.cos(rotation);
        double sin = Math.sin(rotation);

        int[] xPointsRect = {width/2, -width/2, -width/2, width/2};
        int[] yPointsRect = {-height/2, -height/2, height/2, height/2};

        for (int i = 0; i < 4; i++) {
            int xPt = xPointsRect[i];
            int yPt = yPointsRect[i];
            xPointsRect[i] = (int)(xPt*cos - yPt*sin) + xPos;
            yPointsRect[i] = (int)(xPt*sin + yPt*cos) + yPos;
        }

        Polygon rectPoly = new Polygon(xPointsRect, yPointsRect, 4);


        int[] xPointsCanvas = {0,0,screenWidth,screenWidth};
        int[] yPointsCanvas = {0,screenHeight,screenHeight,0};
        Polygon canvasPoly = new Polygon(xPointsCanvas,yPointsCanvas,4);

        Polygon polyUnion;
        
        if (rectPoly.intersects(0, 0, screenWidth, screenHeight)){
            polyUnion = PolyStuff.polygonUnion(rectPoly, canvasPoly);
        } else {
            polyUnion = rectPoly;
            insideCanvas = false;
        }
        
        return polyUnion;
    }

    public Color getAverageColor(BufferedImage img){
        Point topAndBottom = PolyStuff.getTopAndBottom(intersectionPoly);
        PolyStuff.TBD[] lineTopsAndBottoms = PolyStuff.getTopBottomDirection(intersectionPoly);

        double r = 0;
        double g = 0;
        double b = 0;

        int ct = 0;

        for(int y = topAndBottom.x; y < topAndBottom.y; y++){
            Point startAndEnd = PolyStuff.getStartAndEnd(intersectionPoly, y, lineTopsAndBottoms);
            for (int x = startAndEnd.x; x < startAndEnd.y; x++) {
                Color c = new Color(img.getRGB(x, y));
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
                ct++;
            }
        }

        return new Color((int)(r/ct), (int)(g/ct), (int)(b/ct));
    }

    // Static methods

    public static Rect random(BufferedImage targetImg){
        
        int x = (int)(Math.random() * screenWidth);
        int y = (int)(Math.random() * screenHeight);
        int w = SIZE_MIN + (int)(Math.random() * (SIZE_MAX-SIZE_MIN));
        int h = SIZE_MIN + (int)(Math.random() * (SIZE_MAX-SIZE_MIN));
        double rot = Math.random() * 2 * Math.PI; 

        return new Rect(x, y, w, h, rot, targetImg);
    }

    public static double getRandomInRange(double origin, double min, double max, double mutationAmt){
        double localRange = (max-min) * mutationAmt;

        double newNum = origin + (Math.random() * 2 - 1) * localRange;

        if (newNum < min) {
            newNum = min;
        } 
        if (newNum > max) {
            newNum = max;
        }
        //System.out.println(Double.toString(newNum-origin) + ": " + Double.toString(max));
        return newNum;
    }

    public static void setScreenSize(int screenWidth_, int screenHeight_){
        screenWidth = screenWidth_;
        screenHeight = screenHeight_;
    }

    @Override
    public Rect clone(){
        return new Rect(xPos, yPos, width, height, rotation, targetImg);
    }

    // Getters and setters

    public Polygon getIntesectionPoly(){
        return intersectionPoly;
    }

    public boolean isInsideCanvas(){
        return insideCanvas;
    }

}