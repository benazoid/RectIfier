package org.yourcompany.yourproject;

import javax.swing.JFrame;

public class ScreenStuff {
    

    JFrame frame;
    Panel screenPanel;

    public ScreenStuff(Panel mainPanel){

        //screenPanel = new Panel(width, height);

        frame = new JFrame();
        //frame.setUndecorated(true);

        frame.addWindowListener(new Closer());
        frame.setVisible(true);

        mainPanel.setVisible(true);
        mainPanel.setLocation(0, 0);

        frame.setSize(mainPanel.width, mainPanel.height);

        frame.add(mainPanel);

    }

    public void paint(){
        screenPanel.repaint();
    }

    public void addRect(Rect r){
        screenPanel.addRect(r);
    }

    private static class Closer extends java.awt.event.WindowAdapter 
    {   
        public void windowClosing (java.awt.event.WindowEvent e) 
        {   System.exit (0);
        }   //======================
    }
    
}

// public void log(){ System.out.println(Integer.toString(xPos) + ", " + Integer.toString(yPos) + ", " + Integer.toString(width) + ", " +);}
