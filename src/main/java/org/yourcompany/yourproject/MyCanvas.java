package org.yourcompany.yourproject;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;


public class MyCanvas {

    private final BufferedImage targetImage;

    private double[][] baseScoreArr;

    private final BufferedImage currentImage;


    public MyCanvas(BufferedImage targetImage_){
        targetImage = targetImage_;

        currentImage = new BufferedImage(targetImage.getWidth(), targetImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < currentImage.getWidth(); i++) {
            for (int j = 0; j < currentImage.getHeight(); j++) {
                currentImage.setRGB(i, j, (Color.white).getRGB());
            }
        }
    }

    
    public void updateBaseScoreArr(){
        double[][] diff = subRealImages(currentImage, targetImage);
        baseScoreArr = diff;
    }

    public void addToCurrentImage(Rect r){
        Point topAndBottom = PolyStuff.getTopAndBottom(r.getIntesectionPoly());
        PolyStuff.TBD[] lineTopsAndBottoms = PolyStuff.getTopBottomDirection(r.getIntesectionPoly());
        for(int y = topAndBottom.x; y < topAndBottom.y; y++){
            Point startAndEnd = PolyStuff.getStartAndEnd(r.getIntesectionPoly(), y, lineTopsAndBottoms);
            for (int x = startAndEnd.x; x < startAndEnd.y; x++) {
                Color c = new Color(r.red, r.green, r.blue);
                currentImage.setRGB(x, y, c.getRGB());
            }
        }
    }

    // Static methods

    public static double colorDifRGB(int rgb1, int rgb2){
        Color c1 = new Color(rgb1);
        Color c2 = new Color(rgb2);

        float r = (c1.getRed()-c2.getRed());
        float g = (c1.getGreen()-c2.getGreen());
        float b = (c1.getBlue()-c2.getBlue());

        return (r*r + g*g + b*b);
    }

    public static double[][] subRealImages(BufferedImage img1, BufferedImage img2){

        double[][] outArr = new double[img1.getWidth()][img1.getHeight()];

        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {

                int thisRGB = img1.getRGB(x, y);
                int otherRGB = img2.getRGB(x, y);

                outArr[x][y] = colorDifRGB(thisRGB, otherRGB);
            }
        }
        return outArr;

    }
    
    public static double sumArr(double[][] arr){
        double sum = 0;
        for (int x = 0; x < arr.length; x++) {
            for (int y = 0; y < arr[0].length; y++) {
                sum += arr[x][y];
            }
        }
        return sum;
    }    

    public static BufferedImage imgFromArr(double[][] arr){
        BufferedImage img = new BufferedImage(arr.length, arr[0].length, BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < img.getWidth(); x++){
            for(int y = 0; y < img.getHeight(); y++){
                int colorVal = (int)(arr[x][y]*255);
                Color c = new Color(colorVal, colorVal, colorVal);
                img.setRGB(x, y, c.getRGB());
            }
        }
        return img;
    }

    public static double[][] subtractArrays(double[][] arr1, double[][] arr2){
        double[][] outArr = new double[arr1.length][arr1[0].length];
        for (int i = 0; i < arr1.length; i++) {
            for (int j = 0; j < arr1[0].length; j++) {
                outArr[i][j] = Math.abs(arr1[i][j]-arr2[i][j]);
            }
        }
        return outArr;
    }

    // Getters and Setters

    public BufferedImage getCurrentImage(){
        return currentImage;
    }

    public double[][] getBaseScoreArr(){
        return baseScoreArr;
    }

    public BufferedImage getTargetImage(){
        return targetImage;
    }

}
