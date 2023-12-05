package edu.oswego.cs;

import javax.swing.*;
import java.awt.*;

public class MetalAlloyView {

    private final JFrame metalAlloyFrame;
    private final int DEFAULT_REGION_SIZE = 7;

    public MetalAlloyView(int height, int width) {
        metalAlloyFrame = new JFrame("Metal Alloy");
        int taskBarHeight = Toolkit.getDefaultToolkit().getScreenInsets(metalAlloyFrame.getGraphicsConfiguration()).top + 10;
        metalAlloyFrame.setSize(DEFAULT_REGION_SIZE * width, DEFAULT_REGION_SIZE * height + taskBarHeight);
        metalAlloyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        metalAlloyFrame.setResizable(false);
    }

    public void displayRegions(MetalAlloy alloy) {
        metalAlloyFrame.getContentPane().removeAll();
        DrawRegions regions = new DrawRegions(alloy);
        metalAlloyFrame.getContentPane().add(regions);
        metalAlloyFrame.revalidate();
        metalAlloyFrame.repaint();
    }

    public void display() {
        metalAlloyFrame.setVisible(true);
    }


    private class DrawRegions extends JPanel {

        MetalAlloy alloy;

        public DrawRegions(MetalAlloy metalAlloy) {
            this.alloy = metalAlloy;
        }

        public void drawRegions(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            for (int row = 0; row < alloy.getHeight(); row++) {
                for (int col = 0; col < alloy.getWidth(); col++) {
                    MetalAlloyRegion region = alloy.getMetalAlloyRegion(row, col);
                    Color regionColor = new Color(region.getR(), region.getG(), region.getB());
                    graphics2D.setColor(regionColor);
                    graphics2D.fillRect(col * DEFAULT_REGION_SIZE, row * DEFAULT_REGION_SIZE, DEFAULT_REGION_SIZE, DEFAULT_REGION_SIZE);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            drawRegions(graphics);
        }
    }
}
