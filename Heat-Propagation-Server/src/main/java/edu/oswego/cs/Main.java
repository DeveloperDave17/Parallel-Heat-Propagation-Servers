package edu.oswego.cs;

public class Main {

    private static final int DEFAULT_PORT_NUMBER = 26941;

    public static void main(String[] args) {
        final int PORT_NUMBER;
        if (args.length > 0) {
            PORT_NUMBER = Integer.parseInt(args[0]);
        } else {
            PORT_NUMBER = DEFAULT_PORT_NUMBER;
        }
        ParallelCalculationService pcs = new ParallelCalculationService(PORT_NUMBER);
        pcs.startService();
    }
}
