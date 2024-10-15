package org.yourcompany.yourproject;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

public final class Rect {

    static MyCanvas mainCanvas;
    static int screenWidth;
    static int screenHeight;
    
    final int xPos;
    final int yPos;
    final int width;
    final int height;
    final double rotation;
    final int red;
    final int green;
    final int blue;

    Polygon intersectionPoly;
    boolean insideCanvas = true;

    double score = Math.PI; // pi here is very arbitrary

    private double area = -1;

    final static int sizeMax = 50;
    final static int sizeMin = 5;

    public Rect(int x, int y, int w, int h, double rot, int r, int g, int b){
        xPos = x;
        yPos = y;
        width = w;
        height = h;
        rotation = rot;

        red = r;
        green = g;
        blue = b;

        intersectionPoly = getCanvasIntersection();
    }

    public double getScore(BufferedImage targetImage, double[][] baseScoreArr){
        if (score != Math.PI) {
            return score;
        }

        Point topAndBottom = PolyStuff.getTopAndBottom(intersectionPoly);
        PolyStuff.TBD[] lineTopsAndBottoms = PolyStuff.getTopBottomDirection(intersectionPoly);

        Color rectColor = new Color(red, green, blue);

        double rectScore = 0;
        double baseScore = 0;

        for(int y = topAndBottom.x; y < topAndBottom.y; y++){
            Point startAndEnd = PolyStuff.getStartAndEnd(intersectionPoly, y, lineTopsAndBottoms);
            for (int x = startAndEnd.x; x < startAndEnd.y; x++) {
                rectScore += MyCanvas.colorDifRGB(rectColor.getRGB(), targetImage.getRGB(x, y));
                baseScore += baseScoreArr[x][y];
            }
        }
        score = (baseScore - rectScore);

        return score;
    }

    public int compareTo(Rect other){
        //double diff = -(mainCanvas.scoreLocal(this)-mainCanvas.scoreLocal(other));
        BufferedImage targetImage = mainCanvas.targetImage;
        double[][] baseScoreArr = mainCanvas.baseScoreArr;
        double diff = -(this.getScore(targetImage, baseScoreArr)-other.getScore(targetImage, baseScoreArr));
        return (int) Math.signum(diff);
    }

    public static Rect random(){
        

        int x = (int)(Math.random() * screenWidth);
        int y = (int)(Math.random() * screenHeight);
        int w = sizeMin + (int)(Math.random() * (sizeMax-sizeMin));
        int h = sizeMin + (int)(Math.random() * (sizeMax-sizeMin));
        double rot = Math.random() * 2 * Math.PI; 
        int r = (int)(Math.random() * 255);
        int g = (int)(Math.random() * 255);
        int b = (int)(Math.random() * 255);
        return new Rect(x, y, w, h, rot, r, g, b);
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

    // mutationAmt is the % of the full range available to mutate to
    public Rect makeMutatedChild(double mutationAmt){
        int x = (int) getRandomInRange(xPos, 0, screenWidth, mutationAmt);
        int y = (int) getRandomInRange(yPos, 0, screenHeight, mutationAmt);

        int w = (int) getRandomInRange(width, sizeMin, sizeMax, mutationAmt);
        int h = (int) getRandomInRange(width, sizeMin, sizeMax, mutationAmt);

        double rot  = getRandomInRange(rotation, 0, 2*Math.PI, mutationAmt);

        int r = (int) getRandomInRange(red, 0, 255, mutationAmt);
        int g = (int) getRandomInRange(green, 0, 255, mutationAmt);
        int b = (int) getRandomInRange(blue, 0, 255, mutationAmt);
        
        return new Rect(x, y, w, h, rot, r, g, b);
    }

    public static void setmainCanvas(MyCanvas p){
        Rect.mainCanvas = p;
    }

    public static void setScreenSize(int screenWidth_, int screenHeight_){
        screenWidth = screenWidth_;
        screenHeight = screenHeight_;
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

    public double getArea(){
        if (area >= 0) {
            return area;
        }
        area =  PolyStuff.getArea(intersectionPoly);
        return area;
    }

}