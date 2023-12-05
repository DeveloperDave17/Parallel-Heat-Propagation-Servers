package edu.oswego.cs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ParallelCalculationThread extends Thread {

    private Socket socket;

    public ParallelCalculationThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
            String stringQuadrantToCalc = (String) in.readObject();
            Quadrant quadrantToCalc;
            if (stringQuadrantToCalc.equals("TOP_LEFT")) {
                quadrantToCalc = Quadrant.TOP_LEFT;
            } else if (stringQuadrantToCalc.equals("TOP")) {
                quadrantToCalc = Quadrant.TOP;
            } else if (stringQuadrantToCalc.equals("TOP_RIGHT")) {
                quadrantToCalc = Quadrant.TOP_RIGHT;
            } else if (stringQuadrantToCalc.equals("LEFT")) {
                quadrantToCalc = Quadrant.LEFT;
            } else if (stringQuadrantToCalc.equals("MIDDLE")) {
                quadrantToCalc = Quadrant.MIDDLE;
            } else if (stringQuadrantToCalc.equals("RIGHT")) {
                quadrantToCalc = Quadrant.RIGHT;
            } else if (stringQuadrantToCalc.equals("BOTTOM_LEFT")) {
                quadrantToCalc = Quadrant.BOTTOM_LEFT;
            } else if (stringQuadrantToCalc.equals("BOTTOM")) {
                quadrantToCalc = Quadrant.BOTTOM;
            } else if (stringQuadrantToCalc.equals("BOTTOM_RIGHT")) {
                quadrantToCalc = Quadrant.BOTTOM_RIGHT;
            } else {
                quadrantToCalc = Quadrant.ALL;
            }
            MetalAlloy metalAlloyRecieved = (MetalAlloy) in.readObject();
            MetalAlloy metalAlloyResult = new MetalAlloy(metalAlloyRecieved.getHeight(), metalAlloyRecieved.getWidth(), metalAlloyRecieved.getC1(), metalAlloyRecieved.getC2(), metalAlloyRecieved.getC3());
            metalAlloyRecieved.calculateQuadrant(metalAlloyResult, quadrantToCalc);
            out.writeObject(metalAlloyResult);
            out.flush();
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
