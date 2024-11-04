package org.yourcompany.yourproject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class AniGen {
    
    String sheetName = "images/runningSheet.jpg";
    int framesWide = 4;
    int framesTall = 2;

    public AniGen(){
        BufferedImage sheetImg = getImage(sheetName);

        BufferedImage[] sprites = new BufferedImage[framesWide*framesTall];
        int spriteWidth = sheetImg.getWidth()/framesWide;
        int spriteHeight = sheetImg.getHeight()/framesTall;
        int index = 0;
        for (int row = 0; row < framesTall; row++) {
            for (int col = 0; col < framesWide; col++) {
                sprites[index] = sheetImg.getSubimage(col*spriteWidth, row*spriteHeight, spriteWidth, spriteHeight);
                index++;
            }
        }

        PicGen2 picGen = new PicGen2(sprites[0]);
        while (picGen.isLoading()) { 
            // do nothing
        }

        try {
            File outputfile = new File("outImage.png");
            ImageIO.write(picGen.getOutImage(), "png", outputfile);
        } catch (IOException e) {
            // handle exception
        }

        

    }

    public ArrayList<Rect> nextFrame(ArrayList<Rect> rectList_, BufferedImage targetImage){
        final int genSize = 50;
        final double mutationAmt = 0.05;

        double[][] baseScoreArr = MyCanvas.subRealImages(PicGen2.renderImage(genSize, genSize, rectList_), targetImage);
        
        ArrayList<Rect> newRectList = new ArrayList<>();
        for (Rect r : rectList_) {
            ArrayList<Rect> contestants = new ArrayList<>();
            for (int i = 0; i < genSize; i++) {
                contestants.add(r.makeMutatedChild(mutationAmt));
            }
            
            runGeneration(contestants, baseScoreArr, targetImage);
        }
        return newRectList;
    }

    public void runGeneration(ArrayList<Rect> contestants, double[][] baseScoreArr, BufferedImage targetImg){
        contestants.sort((r1,r2)->r1.compareTo(r2, baseScoreArr));

        final double percentage = 0.5;
        int genSize = contestants.size();
        while (contestants.size() > genSize * (1-percentage)) { 
            contestants.remove(contestants.size()-1);
        }
        repopulate(contestants, percentage, targetImg);
    }

    private void repopulate(ArrayList<Rect> contestants, double mutationAmt, BufferedImage targetImg){

        final int ogSize = contestants.size();

        int randomIndex = 0;
        while (contestants.size() < ogSize) { 
            Rect newRect = contestants.get(randomIndex).makeMutatedChild(mutationAmt);
            contestants.add(newRect);
            randomIndex = (int)(Math.random() * ogSize);
        }

        for (int i = 0; i < 5; i++) {
            contestants.add(Rect.random(targetImg));
        }

    }


    public BufferedImage getImage(String filename) {
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
}
