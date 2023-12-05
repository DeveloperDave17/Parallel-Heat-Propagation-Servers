package edu.oswego.cs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final double DEFAULT_S = 6000;
    private static final double DEFAULT_T = 6000;
    private static final double DEFAULT_C1 = 0.75;
    private static final double DEFAULT_C2 = 1.0;
    private static final double DEFAULT_C3 = 1.25;
    private static final int DEFAULT_HEIGHT = 40;
    private static final int DEFAULT_WIDTH = 160;
    private static final int DEFAULT_THRESHOLD = 35000;

    private static final int DEFAULT_SERVER_COUNT = 2;
    private static final int PORT_NUMBER = 26940;

    private static final String HOST_NAME = "localhost";

    private static volatile MetalAlloy alloyToBePainted;

    private static volatile boolean simulationIsActive;

    public static void main(String[] args) throws Exception {
        // How much the top left corner will be heated up at the beginning of each phase
        final double S;
        if (args.length > 0) {
            S = Double.parseDouble(args[0]);
        } else {
            S = DEFAULT_S;
        }
        // How much the bottom right corner will be heated up at the beginning of each phase
        final double T;
        if (args.length > 1) {
            T = Double.parseDouble(args[1]);
        } else {
            T = DEFAULT_T;
        }
        // Thermal constant for metal 1
        final double C1;
        if (args.length > 2) {
            C1 = Double.parseDouble(args[2]);
        } else {
            C1 = DEFAULT_C1;
        }
        // Thermal constant for metal 2
        final double C2;
        if (args.length > 3) {
            C2 = Double.parseDouble(args[3]);
        } else {
            C2 = DEFAULT_C2;
        }
        // Thermal constant for metal 3
        final double C3;
        if (args.length > 4) {
            C3 = Double.parseDouble(args[4]);
        } else {
            C3 = DEFAULT_C3;
        }
        final int HEIGHT;
        if (args.length > 5) {
            HEIGHT = Integer.parseInt(args[5]);
        } else {
            HEIGHT = DEFAULT_HEIGHT;
        }
        final int WIDTH;
        if (args.length > 6) {
            WIDTH = Integer.parseInt(args[6]);
        } else {
            WIDTH = DEFAULT_WIDTH;
        }
        final int THRESHOLD;
        if (args.length > 7) {
            THRESHOLD = Integer.parseInt(args[7]);
        } else {
            THRESHOLD = DEFAULT_THRESHOLD;
        }
        final int SERVER_COUNT;
        if (args.length > 8) {
            SERVER_COUNT= Integer.parseInt(args[8]);
        } else {
            SERVER_COUNT = DEFAULT_SERVER_COUNT;
        }
        runSimulation(S, T, C1, C2, C3, HEIGHT, WIDTH, THRESHOLD, SERVER_COUNT);
    }

    public static void runSimulation(double s, double t, double c1, double c2, double c3, int height, int width, int threshold, int serverCount) throws SecurityException, InterruptedException {
        final MetalAlloy alloyA = new MetalAlloy(height, width, c1, c2, c3);
        // Display Alloy A first
        alloyToBePainted = alloyA;
        final MetalAlloy alloyB = new MetalAlloy(height, width, c1, c2, c3);
        MetalAlloyView metalAlloyView = new MetalAlloyView(height, width);
        metalAlloyView.displayRegions(alloyA);
        metalAlloyView.display();
        ExecutorService displayService = Executors.newFixedThreadPool(1);
        // Activate the display for the simulation
        simulationIsActive = true;
        displayService.submit(() -> {
            while (simulationIsActive) {
                metalAlloyView.displayRegions(alloyToBePainted);
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        alloyA.setTempOfRegion(s, 0, 0);
        alloyA.setTempOfRegion(t, height - 1, width - 1);
        alloyA.getMetalAlloyRegion(0, 0).calcRGB();
        alloyA.getMetalAlloyRegion(height - 1, width - 1).calcRGB();
        alloyA.deepCopyRegionsTo(alloyB);

        // initialize server port list
        List<Integer> ports = new ArrayList<>();
        for (int portIncrement = 1; portIncrement <= serverCount; portIncrement++) {
            int port = PORT_NUMBER + portIncrement;
            ports.add(port);
        }

        PortPool portPool = new PortPool(ports);

        for (int currentIteration = 1; currentIteration <= threshold; currentIteration++) {
            ExecutorService quadrantService = Executors.newFixedThreadPool(9);
            // Swap which alloy is the preOperationAlloy
            boolean useAForPreOp = currentIteration % 2 == 0;
            if (useAForPreOp) {
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyA, Quadrant.TOP_LEFT);
                    result.copyQuadrant(alloyB, Quadrant.TOP_LEFT);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyA, Quadrant.TOP);
                    result.copyQuadrant(alloyB, Quadrant.TOP);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyA, Quadrant.TOP_RIGHT);
                    result.copyQuadrant(alloyB, Quadrant.TOP_RIGHT);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyA, Quadrant.LEFT);
                    result.copyQuadrant(alloyB, Quadrant.LEFT);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyA, Quadrant.MIDDLE);
                    result.copyQuadrant(alloyB, Quadrant.MIDDLE);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyA, Quadrant.RIGHT);
                    result.copyQuadrant(alloyB, Quadrant.RIGHT);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyA, Quadrant.BOTTOM_LEFT);
                    result.copyQuadrant(alloyB, Quadrant.BOTTOM_LEFT);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyA, Quadrant.BOTTOM);
                    result.copyQuadrant(alloyB, Quadrant.BOTTOM);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyA, Quadrant.BOTTOM_RIGHT);
                    result.copyQuadrant(alloyB, Quadrant.BOTTOM_RIGHT);
                });
            } else {
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyB, Quadrant.TOP_LEFT);
                    result.copyQuadrant(alloyA, Quadrant.TOP_LEFT);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyB, Quadrant.TOP);
                    result.copyQuadrant(alloyA, Quadrant.TOP);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyB, Quadrant.TOP_RIGHT);
                    result.copyQuadrant(alloyA, Quadrant.TOP_RIGHT);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyB, Quadrant.LEFT);
                    result.copyQuadrant(alloyA, Quadrant.LEFT);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyB, Quadrant.MIDDLE);
                    result.copyQuadrant(alloyA, Quadrant.MIDDLE);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyB, Quadrant.RIGHT);
                    result.copyQuadrant(alloyA, Quadrant.RIGHT);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyB, Quadrant.BOTTOM_LEFT);
                    result.copyQuadrant(alloyA, Quadrant.BOTTOM_LEFT);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyB, Quadrant.BOTTOM);
                    result.copyQuadrant(alloyA, Quadrant.BOTTOM);
                });
                quadrantService.submit(() -> {
                    CalculationClient client = new CalculationClient(HOST_NAME, portPool.getNextPort());
                    MetalAlloy result = client.processRequest(alloyB, Quadrant.BOTTOM_RIGHT);
                    result.copyQuadrant(alloyA, Quadrant.BOTTOM_RIGHT);
                });
            }
            quadrantService.shutdown();
            quadrantService.awaitTermination(1, TimeUnit.SECONDS);
            // Display updates
            if (useAForPreOp) {
                alloyToBePainted = alloyB;
            } else {
                alloyToBePainted = alloyA;
            }
        }
        // Inform the gui that it no longer needs to refresh
        simulationIsActive = false;
        displayService.shutdown();
        displayService.awaitTermination(1, TimeUnit.SECONDS);
    }
}
