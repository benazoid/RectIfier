package org.yourcompany.yourproject;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public final class PicGen2 {

    private final double imgDownScale = 1;
    private BufferedImage targetImg;

    private final MyCanvas mainCanvas;

    private ArrayList<Rect> contestants = new ArrayList<>();
    private ArrayList<Rect> rectList = new ArrayList<>();

    private ScreenStuff screen;

    private boolean loading = true;
    private BufferedImage outImg;

    private final int rectAmt = 1000;
    private final int genAmt = 50;
    private final int genSize = 30;
    private final double mutationAmt = 0.5;

    public PicGen2(BufferedImage targetImg_){
        targetImg = targetImg_;

        mainCanvas = new MyCanvas(targetImg);
        Rect.setScreenSize(targetImg.getWidth(), targetImg.getHeight());

        screen = new ScreenStuff(targetImg.getWidth(), targetImg.getHeight());

        run();

    }

    public PicGen2(String targetImgSrc){
        targetImg = scaleImg(getImage(targetImgSrc), imgDownScale);
        
        mainCanvas = new MyCanvas(targetImg);
        Rect.setScreenSize(targetImg.getWidth(), targetImg.getHeight());

        screen = new ScreenStuff(targetImg.getWidth(), targetImg.getHeight());

        run();

    }

    private void run(){
        outerloop:
        for (int rectIndex = 0; rectIndex < rectAmt; rectIndex++) {
            mainCanvas.updateBaseScoreArr();

            // Repopulate contestants with random rects
            contestants.clear();
            while (contestants.size() < genSize) {
                Rect r = Rect.random(targetImg);
                if(r.isInsideCanvas())
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
            mainCanvas.addToCurrentImage(contestants.get(0));

            screen.setCanvasImage(mainCanvas.getCurrentImage());
            
        }

        cleanup();

        System.out.println("done");

        BufferedImage img = mainCanvas.getCurrentImage();
        outImg = img;
        loading = false;

        /*
        try {
            File outputfile = new File("outImage.png");
            ImageIO.write(img, "png", outputfile);
        } catch (IOException e) {
            // handle exception
        }*/
        
    }

    private final double runGeneration(){
        // sort contestants best to worst
        contestants.sort((r1,r2)->r1.compareTo(r2, mainCanvas.getBaseScoreArr()));

        // delete bottom 90%
        final double percentage = 0.5;
        while (contestants.size() > genSize * (1-percentage)) { 
            contestants.remove(contestants.size()-1);
        }

        // have remaining produce as much to refill
        repopulate();
        Rect bestRect = contestants.get(0);
        double score = bestRect.getScore(targetImg, mainCanvas.getBaseScoreArr());

        // repeat
        return score;
    }

    private void repopulate(){

        final int ogSize = contestants.size();

        int randomIndex = 0;
        while (contestants.size() < genSize) { 
            Rect newRect = contestants.get(randomIndex).makeMutatedChild(mutationAmt);
            contestants.add(newRect);
            randomIndex = (int)(Math.random() * ogSize);
        }

        for (int i = 0; i < 5; i++) {
            contestants.add(Rect.random(targetImg));
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
            
            Point topAndBottom = PolyStuff.getTopAndBottom(r.getIntesectionPoly());
            PolyStuff.TBD[] lineTopsAndBottoms = PolyStuff.getTopBottomDirection(r.getIntesectionPoly());

            boolean remove = true;
            for(int y = topAndBottom.x; y < topAndBottom.y; y++){
                Point startAndEnd = PolyStuff.getStartAndEnd(r.getIntesectionPoly(), y, lineTopsAndBottoms);
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
                Point startAndEnd = PolyStuff.getStartAndEnd(r.getIntesectionPoly(), y, lineTopsAndBottoms);
                for (int x = startAndEnd.x; x < startAndEnd.y; x++) {
                    layersArr[x][y]--;
                }
            }

            rectList.remove(rectList.size()-i-1);
            i--;
        }

    }

    public static BufferedImage renderImage(int width, int height, ArrayList<Rect> rects){
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (Rect r : rects) {
            Point topAndBottom = PolyStuff.getTopAndBottom(r.getIntesectionPoly());
            PolyStuff.TBD[] lineTopsAndBottoms = PolyStuff.getTopBottomDirection(r.getIntesectionPoly());
            for(int y = topAndBottom.x; y < topAndBottom.y; y++){
                Point startAndEnd = PolyStuff.getStartAndEnd(r.getIntesectionPoly(), y, lineTopsAndBottoms);
                for (int x = startAndEnd.x; x < startAndEnd.y; x++) {
                    img.setRGB(x, y, r.color.getRGB());
                }
            }
        }

        return img;
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

    public static BufferedImage scaleImg(BufferedImage original, double scale){

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

    public BufferedImage getOutImage(){
        return outImg;
    }

    public boolean isLoading(){
        return loading;
    }


    public static void main(String[] args) {

        PicGen2 ag = new PicGen2("images/cat.jpg");
    }
}
