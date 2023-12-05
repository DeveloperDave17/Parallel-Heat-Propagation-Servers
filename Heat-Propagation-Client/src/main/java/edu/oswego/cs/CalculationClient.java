package edu.oswego.cs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class CalculationClient {

    private String hostName;
    private int port;

    public CalculationClient(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public MetalAlloy processRequest(MetalAlloy alloyToProcess, Quadrant quadrantToProcess) {
        MetalAlloy result;
        try (
                Socket socket = new Socket(hostName, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ) {
            out.writeObject(quadrantToProcess.name());
            out.flush();
            out.writeObject(alloyToProcess);
            out.flush();
            result = (MetalAlloy)in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            result = alloyToProcess;
        }
        return result;
    }
}
