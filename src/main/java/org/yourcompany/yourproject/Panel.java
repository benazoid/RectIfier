package org.yourcompany.yourproject;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

public class Panel extends JPanel{

    static BufferedImage targetImage;

    double baseScore;

    double[][] baseScoreArr;

    ArrayList<Rect> panelRects = new ArrayList<>();

    int width;
    int height;

    public Panel(int width_, int height_){
        setPreferredSize(new Dimension(width_, height_));

        width = width_;
        height = height_;

    }

    public void paintComponent(Graphics g){

        g = renderImage(g);

    }

    public Graphics renderImage(Graphics g){

        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);

        try {
            for (Rect r : panelRects) {
                r.draw(g);
            }
            
        } catch (java.util.ConcurrentModificationException e) {
            // loser
        }

        return g;
    }

    public void cleanup(){
        int[][] layersArr = new int[width][height];
        for (int i = 0; i < layersArr.length; i++) {
            for (int j = 0; j < layersArr[i].length; j++) {
                layersArr[i][j] = 0;
            }
        }
        for (int i = 0; i < panelRects.size(); i++) {
            Rect r = panelRects.get(panelRects.size()-i-1);
            
            Point topAndBottom = PolyStuff.getTopAndBottom(r.intersectionPoly);
            PolyStuff.TBD[] lineTopsAndBottoms = PolyStuff.getTopBottomDirection(r.intersectionPoly);


            boolean canRemove = true;
            for(int y = topAndBottom.x; y < topAndBottom.y; y++){
                Point startAndEnd = PolyStuff.getStartAndEnd(r.intersectionPoly, y, lineTopsAndBottoms);
                for (int x = startAndEnd.x; x < startAndEnd.y; x++) {
                    if (layersArr[x][y] == 0) {
                        canRemove = false;
                    }
                    
                    layersArr[x][y]++;
                }
            }

            if (!canRemove) {
                continue;
            }

            for(int y = topAndBottom.x; y < topAndBottom.y; y++){
                Point startAndEnd = PolyStuff.getStartAndEnd(r.intersectionPoly, y, lineTopsAndBottoms);
                for (int x = startAndEnd.x; x < startAndEnd.y; x++) {
                    layersArr[x][y]--;
                }
            }

            panelRects.remove(i);
            i--;

        }

        outerloop:
        for (int i = 0; i < panelRects.size(); i++) {
            Rect r = panelRects.get(panelRects.size()-i-1);

            Point topAndBottom = PolyStuff.getTopAndBottom(r.intersectionPoly);
            PolyStuff.TBD[] lineTopsAndBottoms = PolyStuff.getTopBottomDirection(r.intersectionPoly);

            

            for(int y = topAndBottom.x; y < topAndBottom.y; y++){
                Point startAndEnd = PolyStuff.getStartAndEnd(r.intersectionPoly, y, lineTopsAndBottoms);
                for (int x = startAndEnd.x; x < startAndEnd.y; x++) {
                    layersArr[x][y]--;
                }
            }

            panelRects.remove(i);
            i--;
        }
    }

    public double score(Rect rect){

        double score1 = baseScore; // Starts really low, gets higher

        double score2 = rect.getScore(targetImage); // hopefully higher than score1

        return score1-score2;
        
    }

    public double scoreLocal(Rect rect){

        return rect.getNewScore(targetImage, baseScoreArr);

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

    static final double COLOR_SPACE_SIZE = Math.sqrt(3*255*255);

    public static double colorDifRGB(int rgb1, int rgb2){
        Color c1 = new Color(rgb1);
        Color c2 = new Color(rgb2);

        float r = (c1.getRed()-c2.getRed());
        float g = (c1.getGreen()-c2.getGreen());
        float b = (c1.getBlue()-c2.getBlue());

        return (r*r + g*g + b*b);
    }

    public BufferedImage createBaseImage() {

        this.repaint();

        int w = this.width;
        int h = this.height;

        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();

        this.paint(g);
        g.dispose();

        return bi;
        
    }

    public BufferedImage createImageWithRect(Rect rect){

        this.repaint();

        int w = this.width;
        int h = this.height;
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        this.paint(g);
        rect.draw(g);
        g.dispose();
        return bi;
    }

    public double findBaseScore(){
        double[][] diff = subRealImages(createBaseImage(), targetImage);
        baseScoreArr = diff;
        double sum = sumArr(diff);
        baseScore = sum;
        return sum;
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

    public static void setTargetImage(BufferedImage img){
        Panel.targetImage = img;
    }

    public void addRect(Rect r){
        panelRects.add(r);
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

}
