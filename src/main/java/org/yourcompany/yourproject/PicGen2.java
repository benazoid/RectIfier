package org.yourcompany.yourproject;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class PicGen2 {

    String targetImgSrc = "images/cat.jpg";
    double imgDownScale = 1;
    BufferedImage targetImg;

    MyCanvas mainPanel;

    ArrayList<Rect> contestants = new ArrayList<>();
    ArrayList<Rect> rectList = new ArrayList<>();

    ScreenStuff screen;

    final int rectAmt = 5000;
    final int genAmt = 50;
    final int genSize = 30;
    final double mutationAmt = 0.5;

    public PicGen2(){
        targetImg = scaleImg(getImage(targetImgSrc), imgDownScale);

        mainPanel = new MyCanvas(targetImg);
        Rect.setMainPanel(mainPanel);
        Rect.setScreenSize(targetImg.getWidth(), targetImg.getHeight());

        screen = new ScreenStuff(targetImg.getWidth(), targetImg.getHeight());

        run();

    }     

    public void run(){
        outerloop:
        for (int rectIndex = 0; rectIndex < rectAmt; rectIndex++) {
            mainPanel.updateBaseScoreArr();

            // Repopulate contestants with random rects
            contestants.clear();
            while (contestants.size() < genSize) {
                Rect r = Rect.random();
                if(r.insidePanel)
                    contestants.add(r);
            }
            
            int ct = 0;
            double score = 0;
            while (ct < genAmt) {
                score = runGeneration();

                if (ct > 100) {
                    continue outerloop;
                }
                ct++;
            }

            // If the rect doesn't add value to the image, move on
            if (score < 0) {
                continue;
            }

            rectList.add(contestants.get(0));
            mainPanel.addToCurrentImage(contestants.get(0));

            screen.setCanvasImage(mainPanel.currentImage);
            
        }

        cleanup();

        System.out.println("done");

        BufferedImage img = mainPanel.currentImage;

        try {
            File outputfile = new File("outImage.jpg");
            ImageIO.write(img, "jpg", outputfile);
        } catch (IOException e) {
            // handle exception
        }
        
    }

    public final double runGeneration(){
        // sort contestants best to worst
        contestants.sort((r1,r2)->r1.compareTo(r2));

        // delete bottom 90%
        final double percentage = 0.5;
        while (contestants.size() > genSize * (1-percentage)) { 
            contestants.remove(contestants.size()-1);
        }

        // have remaining produce as much to refill
        repopulate();
        Rect bestRect = contestants.get(0);
        double score = bestRect.getScore(targetImg, mainPanel.baseScoreArr);

        // repeat
        return score;
    }

    public void repopulate(){

        final int ogSize = contestants.size();

        int randomIndex = 0;
        while (contestants.size() < genSize) { 
            Rect newRect = contestants.get(randomIndex).makeMutatedChild(mutationAmt);
            contestants.add(newRect);
            randomIndex = (int)(Math.random() * ogSize);
        }

        for (int i = 0; i < 5; i++) {
            contestants.add(Rect.random());
        }

    }

    // Removes rectangles that are being completely covered by other rectangles
    private void cleanup(){
        int[][] layersArr = new int[targetImg.getWidth()][targetImg.getHeight()];
        for (int i = 0; i < layersArr.length; i++) {
            for (int j = 0; j < layersArr[i].length; j++) {
                layersArr[i][j] = 0;
            }
        }
        for (int i = 0; i < rectList.size(); i++) {
            Rect r = rectList.get(rectList.size()-i-1);
            
            Point topAndBottom = PolyStuff.getTopAndBottom(r.intersectionPoly);
            PolyStuff.TBD[] lineTopsAndBottoms = PolyStuff.getTopBottomDirection(r.intersectionPoly);

            boolean remove = true;
            for(int y = topAndBottom.x; y < topAndBottom.y; y++){
                Point startAndEnd = PolyStuff.getStartAndEnd(r.intersectionPoly, y, lineTopsAndBottoms);
                for (int x = startAndEnd.x; x < startAndEnd.y; x++) {
                    // If there's nothing behind it
                    if (layersArr[x][y] == 0) {
                        remove = false;
                    }
                    
                    layersArr[x][y]++;
                }
            }

            if (!remove) {
                continue;
            }

            for(int y = topAndBottom.x; y < topAndBottom.y; y++){
                Point startAndEnd = PolyStuff.getStartAndEnd(r.intersectionPoly, y, lineTopsAndBottoms);
                for (int x = startAndEnd.x; x < startAndEnd.y; x++) {
                    layersArr[x][y]--;
                }
            }

            rectList.remove(rectList.size()-i-1);
            i--;
        }

    }

    private BufferedImage getImage(String filename) {
        // This time, you can use an InputStream to load
        try {
            // Grab the InputStream for the image.                    
            InputStream in = getClass().getResourceAsStream(filename);

            // Then read it.
            return ImageIO.read(in);
        } catch (IOException e) {
            System.out.println("The image was not loaded.");
            //System.exit(1);
        }

        return null;
    }

    private BufferedImage scaleImg(BufferedImage original, double scale){

        int newWidth = (int)(original.getWidth() * scale);
        int newHeight = (int)(original.getHeight() * scale);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, original.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, 0, 0, original.getWidth(),
        original.getHeight(), null);
        g.dispose();

        return resized;

    }


    public static void main(String[] args) {
        PicGen2 pg2 = new PicGen2();
    }
}
