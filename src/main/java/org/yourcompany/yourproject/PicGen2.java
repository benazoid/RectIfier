package org.yourcompany.yourproject;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class PicGen2 {

    String targetImgSrc = "images/snapeDad.JPG";
    double imgDownScale = 0.15;
    BufferedImage targetImg;

    Panel mainPanel;

    ArrayList<Rect> contestants = new ArrayList<>();

    ScreenStuff screen;

    final int rectAmt = 30000;
    final int genAmt = 50;
    final int genSize = 30;
    final double mutationAmt = 0.5;
    final double scoreAreaLimit = 0;

    FileWriter myWriter;

    public PicGen2(){
        targetImg = scaleImg(getImage(targetImgSrc), imgDownScale);

        Panel.setTargetImage(targetImg);
        mainPanel = new Panel(targetImg.getWidth(), targetImg.getHeight());
        Rect.setMainPanel(mainPanel);
        Rect.setScreenSize(targetImg.getWidth(), targetImg.getHeight());

        screen = new ScreenStuff(mainPanel);

        run();

    }     

    public void run(){
        outerloop:
        for (int rectIndex = 0; rectIndex < rectAmt; rectIndex++) {
            mainPanel.findBaseScore();

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

            if (score < 0) {
                continue;
            }

            mainPanel.addRect(contestants.get(0));

            
        }

        System.out.println("done");

        BufferedImage img = mainPanel.createBaseImage();


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
        double score = mainPanel.scoreLocal(bestRect);

        // repeat
        return score;
    }

    public void repopulate(){

        final int ogSize = contestants.size();

        /*int randomIndex = 0;
        while (contestants.size() < genSize) { 
            Rect newRect = contestants.get(randomIndex).makeMutatedChild(mutationAmt);
            contestants.add(newRect);
            randomIndex = (int)(Math.random() * ogSize);
        }*/

        for (int i = 0; i < ogSize-5; i++) {
            Rect newRect = contestants.get(i).makeMutatedChild(mutationAmt);
            contestants.add(newRect);
        }

        for (int i = 0; i < 5; i++) {
            contestants.add(Rect.random());
        }

    }

    public static void main(String[] args) {
        PicGen2 pg2 = new PicGen2();
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


}
