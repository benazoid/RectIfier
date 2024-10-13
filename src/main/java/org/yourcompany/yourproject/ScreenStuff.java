package org.yourcompany.yourproject;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ScreenStuff extends JPanel{
    
    JFrame frame;

    BufferedImage canvasImage;

    public ScreenStuff(int width_, int height_){
        super();

        this.setPreferredSize(new Dimension(width_, height_));
        canvasImage = new BufferedImage(width_, height_, BufferedImage.TYPE_INT_RGB);

        frame = new JFrame();

        frame.setUndecorated(true);


        frame.addWindowListener(new Closer());
        frame.setVisible(true);

        frame.setSize(width_, height_);

        frame.add(this);

    }

    public void setCanvasImage(BufferedImage img){
        canvasImage = img;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        g.drawImage(canvasImage, 0, 0, this);

        System.out.println("draw");
    }

    private static class Closer extends java.awt.event.WindowAdapter 
    {   
        public void windowClosing (java.awt.event.WindowEvent e) 
        {   System.exit (0);
        }   //======================
    }
    
}
