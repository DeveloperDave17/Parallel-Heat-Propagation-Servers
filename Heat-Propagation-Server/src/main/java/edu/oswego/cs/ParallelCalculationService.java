package edu.oswego.cs;

import java.io.IOException;
import java.net.ServerSocket;

public class ParallelCalculationService {

    private final int PORT_NUMBER;

    private volatile boolean listening = true;

    public ParallelCalculationService(int portNumber) {
        PORT_NUMBER = portNumber;
    }

    public void startService() {
        try (ServerSocket serverSocket = new ServerSocket(PORT_NUMBER)) {
            while (listening) {
               new ParallelCalculationThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
