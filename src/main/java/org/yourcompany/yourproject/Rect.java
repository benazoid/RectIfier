package org.yourcompany.yourproject;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

public final class Rect {

    final static boolean AUTO_COLOR = true;

    static MyCanvas mainCanvas;
    static int screenWidth;
    static int screenHeight;

    final static int SIZE_MAX = 50;
    final static int SIZE_MIN = 5;
    
    final int xPos;
    final int yPos;
    final int width;
    final int height;
    final double rotation;
    final int red;
    final int green;
    final int blue;

    private Polygon intersectionPoly;
    private boolean insideCanvas = true;

    private double score = Math.PI; // pi here is very arbitrary

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

    public Rect(int x, int y, int w, int h, double rot){
        xPos = x;
        yPos = y;
        width = w;
        height = h;
        rotation = rot;

        intersectionPoly = getCanvasIntersection();
        
        Color c = getAverageColor(intersectionPoly, mainCanvas);

        red = c.getRed();
        green = c.getGreen();
        blue = c.getBlue();
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
        BufferedImage targetImage = mainCanvas.getTargetImage();
        double[][] baseScoreArr = mainCanvas.getBaseScoreArr();
        double diff = -(this.getScore(targetImage, baseScoreArr)-other.getScore(targetImage, baseScoreArr));
        return (int) Math.signum(diff);
    }

    // mutationAmt is the % of the full range available to mutate to
    public Rect makeMutatedChild(double mutationAmt){
        int x = (int) getRandomInRange(xPos, 0, screenWidth, mutationAmt);
        int y = (int) getRandomInRange(yPos, 0, screenHeight, mutationAmt);

        int w = (int) getRandomInRange(width, SIZE_MIN, SIZE_MAX, mutationAmt);
        int h = (int) getRandomInRange(width, SIZE_MIN, SIZE_MAX, mutationAmt);

        double rot  = getRandomInRange(rotation, 0, 2*Math.PI, mutationAmt);

        if(AUTO_COLOR){
            return new Rect(x, y, w, h, rot);
        }

        int r = (int) getRandomInRange(red, 0, 255, mutationAmt);
        int g = (int) getRandomInRange(green, 0, 255, mutationAmt);
        int b = (int) getRandomInRange(blue, 0, 255, mutationAmt);
        
        return new Rect(x, y, w, h, rot, r, g, b);
    }

    public static Color getAverageColor(Polygon intersection, MyCanvas canvas){
        Point topAndBottom = PolyStuff.getTopAndBottom(intersection);
        PolyStuff.TBD[] lineTopsAndBottoms = PolyStuff.getTopBottomDirection(intersection);

        float r = 0;
        float g = 0;
        float b = 0;

        int ct = 0;

        for(int y = topAndBottom.x; y < topAndBottom.y; y++){
            Point startAndEnd = PolyStuff.getStartAndEnd(intersection, y, lineTopsAndBottoms);
            for (int x = startAndEnd.x; x < startAndEnd.y; x++) {
                Color c = new Color(canvas.getTargetImage().getRGB(x, y));
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();

                ct++;
            }
        }

        if (ct == 0) {
            return new Color(0,0,0);
        }

        return new Color((int)(r/ct), (int)(g/ct), (int)(b/ct));
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

    // Static methods

    public static Rect random(){
        
        int x = (int)(Math.random() * screenWidth);
        int y = (int)(Math.random() * screenHeight);
        int w = SIZE_MIN + (int)(Math.random() * (SIZE_MAX-SIZE_MIN));
        int h = SIZE_MIN + (int)(Math.random() * (SIZE_MAX-SIZE_MIN));
        double rot = Math.random() * 2 * Math.PI;

        if (AUTO_COLOR) {
            return new Rect(x, y, w, h, rot);
        }

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

    public static void setmainCanvas(MyCanvas p){
        Rect.mainCanvas = p;
    }

    public static void setScreenSize(int screenWidth_, int screenHeight_){
        screenWidth = screenWidth_;
        screenHeight = screenHeight_;
    }

    // Getters and setters

    public Polygon getIntesectionPoly(){
        return intersectionPoly;
    }

    public boolean isInsideCanvas(){
        return insideCanvas;
    }

}