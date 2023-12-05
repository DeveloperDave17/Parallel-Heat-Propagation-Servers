package edu.oswego.cs;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Phaser;

public class MetalAlloy implements Serializable {

    private int height;
    private int width;

    // Thermal Constants
    private double c1;
    private double c2;
    private double c3;

    // Constraints for Tasks
    private final int NUM_CALCULATIONS_PER_THREAD = 60;

    private MetalAlloyRegion[][] metalAlloyRegions;

    public MetalAlloy(int height, int width, double c1, double c2, double c3) {
        this.height = height;
        this.width = width;
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        metalAlloyRegions = new MetalAlloyRegion[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // Initialize all regions of the alloy to have a temperature of 0 degrees Celsius.
                metalAlloyRegions[i][j] = new MetalAlloyRegion(0);
                metalAlloyRegions[i][j].calcRGB();
            }
        }
    }

    public void increaseTempOfRegion(double tempIncrease, int row, int col) {
        metalAlloyRegions[row][col].increaseTemperature(tempIncrease);
    }

    public void setTempOfRegion(double newTemp, int row, int col) {
        metalAlloyRegions[row][col].setTemperature(newTemp);
    }

    public MetalAlloyRegion getMetalAlloyRegion(int row, int col) {
        return metalAlloyRegions[row][col];
    }

    public double calculateNewTempForRegion(int row, int col) {
        // Don't change the temperature of the top left corner
        if (row == col && row == 0) {
            return metalAlloyRegions[row][col].getTemperature();
        }
        // Don't change the temperature of the bottom right corner
        if (row == height - 1 && col == width - 1) {
            return metalAlloyRegions[row][col].getTemperature();
        }
        double metal1TempSummation = getMetalSummation(row, col, 1);
        double metal2TempSummation = getMetalSummation(row, col, 2);
        double metal3TempSummation = getMetalSummation(row, col, 3);
        // finding the number of  neighbors
        int numNeighbors = 0;
        if (row > 0) {
            numNeighbors++;
        }
        if (row < height - 1) {
            numNeighbors++;
        }
        if (col > 0) {
            numNeighbors++;
        }
        if (col < width - 1) {
            numNeighbors++;
        }
        double temperatureOfRegion = 0;
        temperatureOfRegion += c1 * metal1TempSummation / numNeighbors;
        temperatureOfRegion += c2 * metal2TempSummation / numNeighbors;
        temperatureOfRegion += c3 * metal3TempSummation / numNeighbors;
        return temperatureOfRegion;
    }

    /**
     * Takes a metal type of either 1, 2, or 3 since a metal alloy is made up of 3 metals.
     * @return
     */
    public double getMetalSummation(int row, int col, int metalType) {
        double metalSummation = 0;
        if (metalType == 1) {
            if (row > 0) {
                metalSummation += metalAlloyRegions[row - 1][col].getPercentOfMetal1() * metalAlloyRegions[row - 1][col].getTemperature();
            }
            if (row < height - 1) {
                metalSummation += metalAlloyRegions[row + 1][col].getPercentOfMetal1() * metalAlloyRegions[row + 1][col].getTemperature();
            }
            if (col > 0) {
                metalSummation += metalAlloyRegions[row][col - 1].getPercentOfMetal1() * metalAlloyRegions[row][col - 1].getTemperature();
            }
            if (col < width - 1) {
                metalSummation += metalAlloyRegions[row][col + 1].getPercentOfMetal1() * metalAlloyRegions[row][col + 1].getTemperature();
            }
        } else if (metalType == 2) {
            if (row > 0) {
                metalSummation += metalAlloyRegions[row - 1][col].getPercentOfMetal2() * metalAlloyRegions[row - 1][col].getTemperature();
            }
            if (row < height - 1) {
                metalSummation += metalAlloyRegions[row + 1][col].getPercentOfMetal2() * metalAlloyRegions[row + 1][col].getTemperature();
            }
            if (col > 0) {
                metalSummation += metalAlloyRegions[row][col - 1].getPercentOfMetal2() * metalAlloyRegions[row][col - 1].getTemperature();
            }
            if (col < width - 1) {
                metalSummation += metalAlloyRegions[row][col + 1].getPercentOfMetal2() * metalAlloyRegions[row][col + 1].getTemperature();
            }
        } else if (metalType == 3) {
            if (row > 0) {
                metalSummation += metalAlloyRegions[row - 1][col].getPercentOfMetal3() * metalAlloyRegions[row - 1][col].getTemperature();
            }
            if (row < height - 1) {
                metalSummation += metalAlloyRegions[row + 1][col].getPercentOfMetal3() * metalAlloyRegions[row + 1][col].getTemperature();
            }
            if (col > 0) {
                metalSummation += metalAlloyRegions[row][col - 1].getPercentOfMetal3() * metalAlloyRegions[row][col - 1].getTemperature();
            }
            if (col < width - 1) {
                metalSummation += metalAlloyRegions[row][col + 1].getPercentOfMetal3() * metalAlloyRegions[row][col + 1].getTemperature();
            }
        }
        return metalSummation;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void calculateQuadrant(MetalAlloy alloyToStoreResults, Quadrant quadrantToRun) {
        ExecutorService workStealingPool = new ForkJoinPool();
        Phaser phaser = new Phaser(1);
        // used to specify the dimensions of the quadrant.
        int quadrantHeight;
        int quadrantWidth;
        switch (quadrantToRun) {
            case ALL:
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j += NUM_CALCULATIONS_PER_THREAD) {
                        final int ROW = i;
                        final int COL = j;
                        final int COLEND;
                        // Possible exclusive end of the range of columns a task is in charge of (8 cells)
                        int possibleColCalcEnd = j + NUM_CALCULATIONS_PER_THREAD;
                        if (possibleColCalcEnd < width) {
                            COLEND = possibleColCalcEnd;
                        } else {
                            COLEND = width;
                        }
                        phaser.register();
                        workStealingPool.submit(() -> {
                            for (int currentCol = COL; currentCol < COLEND; currentCol++) {
                                double result = calculateNewTempForRegion(ROW, currentCol);
                                alloyToStoreResults.setTempOfRegion(result, ROW, currentCol);
                                alloyToStoreResults.getMetalAlloyRegion(ROW, currentCol).calcRGB();
                            }
                            phaser.arriveAndDeregister();
                        });
                    }
                }
                break;

            case TOP_LEFT:
                quadrantHeight = height / 3;
                quadrantWidth = width / 3;
                for (int i = 0; i < quadrantHeight; i++) {
                    for (int j = 0; j < quadrantWidth; j += NUM_CALCULATIONS_PER_THREAD) {
                        final int ROW = i;
                        final int COL = j;
                        final int COLEND;
                        // Possible exclusive end of the range of columns a task is in charge of (8 cells)
                        int possibleColCalcEnd = j + NUM_CALCULATIONS_PER_THREAD;
                        if (possibleColCalcEnd < width) {
                            COLEND = possibleColCalcEnd;
                        } else {
                            COLEND = width;
                        }
                        phaser.register();
                        workStealingPool.submit(() -> {
                            for (int currentCol = COL; currentCol < COLEND; currentCol++) {
                                double result = calculateNewTempForRegion(ROW, currentCol);
                                alloyToStoreResults.setTempOfRegion(result, ROW, currentCol);
                                alloyToStoreResults.getMetalAlloyRegion(ROW, currentCol).calcRGB();
                            }
                            phaser.arriveAndDeregister();
                        });
                    }
                }
                break;

            case TOP:
                quadrantHeight = height / 3;
                quadrantWidth = 2 * (width / 3);
                for (int i = 0; i < quadrantHeight; i++) {
                    for (int j = width / 3; j <  quadrantWidth; j += NUM_CALCULATIONS_PER_THREAD) {
                        final int ROW = i;
                        final int COL = j;
                        final int COLEND;
                        // Possible exclusive end of the range of columns a task is in charge of (8 cells)
                        int possibleColCalcEnd = j + NUM_CALCULATIONS_PER_THREAD;
                        if (possibleColCalcEnd < width) {
                            COLEND = possibleColCalcEnd;
                        } else {
                            COLEND = width;
                        }
                        phaser.register();
                        workStealingPool.submit(() -> {
                            for (int currentCol = COL; currentCol < COLEND; currentCol++) {
                                double result = calculateNewTempForRegion(ROW, currentCol);
                                alloyToStoreResults.setTempOfRegion(result, ROW, currentCol);
                                alloyToStoreResults.getMetalAlloyRegion(ROW, currentCol).calcRGB();
                            }
                            phaser.arriveAndDeregister();
                        });
                    }
                }
                break;

            case TOP_RIGHT:
                quadrantHeight = height / 3;
                quadrantWidth = width;
                for (int i = 0; i < quadrantHeight; i++) {
                    for (int j = 2 * (width / 3); j < quadrantWidth; j += NUM_CALCULATIONS_PER_THREAD) {
                        final int ROW = i;
                        final int COL = j;
                        final int COLEND;
                        // Possible exclusive end of the range of columns a task is in charge of (8 cells)
                        int possibleColCalcEnd = j + NUM_CALCULATIONS_PER_THREAD;
                        if (possibleColCalcEnd < width) {
                            COLEND = possibleColCalcEnd;
                        } else {
                            COLEND = width;
                        }
                        phaser.register();
                        workStealingPool.submit(() -> {
                            for (int currentCol = COL; currentCol < COLEND; currentCol++) {
                                double result = calculateNewTempForRegion(ROW, currentCol);
                                alloyToStoreResults.setTempOfRegion(result, ROW, currentCol);
                                alloyToStoreResults.getMetalAlloyRegion(ROW, currentCol).calcRGB();
                            }
                            phaser.arriveAndDeregister();
                        });
                    }
                }
                break;

            case LEFT:
                quadrantHeight = 2 * (height / 3);
                quadrantWidth = width / 3;
                for (int i = height / 3; i < quadrantHeight; i++) {
                    for (int j = 0; j < quadrantWidth; j += NUM_CALCULATIONS_PER_THREAD) {
                        final int ROW = i;
                        final int COL = j;
                        final int COLEND;
                        // Possible exclusive end of the range of columns a task is in charge of (8 cells)
                        int possibleColCalcEnd = j + NUM_CALCULATIONS_PER_THREAD;
                        if (possibleColCalcEnd < width) {
                            COLEND = possibleColCalcEnd;
                        } else {
                            COLEND = width;
                        }
                        phaser.register();
                        workStealingPool.submit(() -> {
                            for (int currentCol = COL; currentCol < COLEND; currentCol++) {
                                double result = calculateNewTempForRegion(ROW, currentCol);
                                alloyToStoreResults.setTempOfRegion(result, ROW, currentCol);
                                alloyToStoreResults.getMetalAlloyRegion(ROW, currentCol).calcRGB();
                            }
                            phaser.arriveAndDeregister();
                        });
                    }
                }
                break;

            case MIDDLE:
                quadrantHeight = 2 * (height / 3);
                quadrantWidth = 2 * (width / 3);
                for (int i = height / 3; i < quadrantHeight; i++) {
                    for (int j = width / 3; j < quadrantWidth; j += NUM_CALCULATIONS_PER_THREAD) {
                        final int ROW = i;
                        final int COL = j;
                        final int COLEND;
                        // Possible exclusive end of the range of columns a task is in charge of (8 cells)
                        int possibleColCalcEnd = j + NUM_CALCULATIONS_PER_THREAD;
                        if (possibleColCalcEnd < width) {
                            COLEND = possibleColCalcEnd;
                        } else {
                            COLEND = width;
                        }
                        phaser.register();
                        workStealingPool.submit(() -> {
                            for (int currentCol = COL; currentCol < COLEND; currentCol++) {
                                double result = calculateNewTempForRegion(ROW, currentCol);
                                alloyToStoreResults.setTempOfRegion(result, ROW, currentCol);
                                alloyToStoreResults.getMetalAlloyRegion(ROW, currentCol).calcRGB();
                            }
                            phaser.arriveAndDeregister();
                        });
                    }
                }
                break;

            case RIGHT:
                quadrantHeight = 2 * (height / 3);
                quadrantWidth = width;
                for (int i = height / 3; i < quadrantHeight; i++) {
                    for (int j = 2 * (width / 3); j < quadrantWidth; j += NUM_CALCULATIONS_PER_THREAD) {
                        final int ROW = i;
                        final int COL = j;
                        final int COLEND;
                        // Possible exclusive end of the range of columns a task is in charge of (8 cells)
                        int possibleColCalcEnd = j + NUM_CALCULATIONS_PER_THREAD;
                        if (possibleColCalcEnd < width) {
                            COLEND = possibleColCalcEnd;
                        } else {
                            COLEND = width;
                        }
                        phaser.register();
                        workStealingPool.submit(() -> {
                            for (int currentCol = COL; currentCol < COLEND; currentCol++) {
                                double result = calculateNewTempForRegion(ROW, currentCol);
                                alloyToStoreResults.setTempOfRegion(result, ROW, currentCol);
                                alloyToStoreResults.getMetalAlloyRegion(ROW, currentCol).calcRGB();
                            }
                            phaser.arriveAndDeregister();
                        });
                    }
                }
                break;

            case BOTTOM_LEFT:
                quadrantHeight = height;
                quadrantWidth = width / 3;
                for (int i = 2 * (height / 3); i < quadrantHeight; i++) {
                    for (int j = 0; j < quadrantWidth; j += NUM_CALCULATIONS_PER_THREAD) {
                        final int ROW = i;
                        final int COL = j;
                        final int COLEND;
                        // Possible exclusive end of the range of columns a task is in charge of (8 cells)
                        int possibleColCalcEnd = j + NUM_CALCULATIONS_PER_THREAD;
                        if (possibleColCalcEnd < width) {
                            COLEND = possibleColCalcEnd;
                        } else {
                            COLEND = width;
                        }
                        phaser.register();
                        workStealingPool.submit(() -> {
                            for (int currentCol = COL; currentCol < COLEND; currentCol++) {
                                double result = calculateNewTempForRegion(ROW, currentCol);
                                alloyToStoreResults.setTempOfRegion(result, ROW, currentCol);
                                alloyToStoreResults.getMetalAlloyRegion(ROW, currentCol).calcRGB();
                            }
                            phaser.arriveAndDeregister();
                        });
                    }
                }
                break;

            case BOTTOM:
                quadrantHeight = height;
                quadrantWidth = 2 * (width / 3);
                for (int i = 2 * (height / 3); i < quadrantHeight; i++) {
                    for (int j = width / 3; j < quadrantWidth; j += NUM_CALCULATIONS_PER_THREAD) {
                        final int ROW = i;
                        final int COL = j;
                        final int COLEND;
                        // Possible exclusive end of the range of columns a task is in charge of (8 cells)
                        int possibleColCalcEnd = j + NUM_CALCULATIONS_PER_THREAD;
                        if (possibleColCalcEnd < width) {
                            COLEND = possibleColCalcEnd;
                        } else {
                            COLEND = width;
                        }
                        phaser.register();
                        workStealingPool.submit(() -> {
                            for (int currentCol = COL; currentCol < COLEND; currentCol++) {
                                double result = calculateNewTempForRegion(ROW, currentCol);
                                alloyToStoreResults.setTempOfRegion(result, ROW, currentCol);
                                alloyToStoreResults.getMetalAlloyRegion(ROW, currentCol).calcRGB();
                            }
                            phaser.arriveAndDeregister();
                        });
                    }
                }
                break;

            case BOTTOM_RIGHT:
                quadrantHeight = height;
                quadrantWidth = width;
                for (int i = 2 * (height / 3); i < quadrantHeight; i++) {
                    for (int j = 2 * (width / 3); j < quadrantWidth; j += NUM_CALCULATIONS_PER_THREAD) {
                        final int ROW = i;
                        final int COL = j;
                        final int COLEND;
                        // Possible exclusive end of the range of columns a task is in charge of (8 cells)
                        int possibleColCalcEnd = j + NUM_CALCULATIONS_PER_THREAD;
                        if (possibleColCalcEnd < width) {
                            COLEND = possibleColCalcEnd;
                        } else {
                            COLEND = width;
                        }
                        phaser.register();
                        workStealingPool.submit(() -> {
                            for (int currentCol = COL; currentCol < COLEND; currentCol++) {
                                double result = calculateNewTempForRegion(ROW, currentCol);
                                alloyToStoreResults.setTempOfRegion(result, ROW, currentCol);
                                alloyToStoreResults.getMetalAlloyRegion(ROW, currentCol).calcRGB();
                            }
                            phaser.arriveAndDeregister();
                        });
                    }
                }
                break;
        }
        phaser.arriveAndAwaitAdvance();
    }

    public double getC1() {
        return c1;
    }

    public double getC2() {
        return c2;
    }

    public double getC3() {
        return c3;
    }

    public void copyQuadrant(MetalAlloy alloyToStoreResults, Quadrant targetQuadrant) {
        // used to specify the dimensions of the quadrant.
        int quadrantHeight;
        int quadrantWidth;
        switch (targetQuadrant) {
            case ALL:
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        double tempOfRegion = metalAlloyRegions[i][j].getTemperature();
                        alloyToStoreResults.setTempOfRegion(tempOfRegion, i, j);
                        MetalAlloyRegion region = alloyToStoreResults.getMetalAlloyRegion(i,j);
                        region.copyRegionColors(metalAlloyRegions[i][j]);
                    }
                }
                break;

            case TOP_LEFT:
                quadrantHeight = height / 3;
                quadrantWidth = width / 3;
                for (int i = 0; i < quadrantHeight; i++) {
                    for (int j = 0; j < quadrantWidth; j++) {
                        double tempOfRegion = metalAlloyRegions[i][j].getTemperature();
                        alloyToStoreResults.setTempOfRegion(tempOfRegion, i, j);
                        MetalAlloyRegion region = alloyToStoreResults.getMetalAlloyRegion(i,j);
                        region.copyRegionColors(metalAlloyRegions[i][j]);
                    }
                }
                break;

            case TOP:
                quadrantHeight = height / 3;
                quadrantWidth = 2 * (width / 3);
                for (int i = 0; i < quadrantHeight; i++) {
                    for (int j = width / 3; j <  quadrantWidth; j++) {
                        double tempOfRegion = metalAlloyRegions[i][j].getTemperature();
                        alloyToStoreResults.setTempOfRegion(tempOfRegion, i, j);
                        MetalAlloyRegion region = alloyToStoreResults.getMetalAlloyRegion(i,j);
                        region.copyRegionColors(metalAlloyRegions[i][j]);
                    }
                }
                break;

            case TOP_RIGHT:
                quadrantHeight = height / 3;
                quadrantWidth = width;
                for (int i = 0; i < quadrantHeight; i++) {
                    for (int j = 2 * (width / 3); j < quadrantWidth; j ++) {
                        double tempOfRegion = metalAlloyRegions[i][j].getTemperature();
                        alloyToStoreResults.setTempOfRegion(tempOfRegion, i, j);
                        MetalAlloyRegion region = alloyToStoreResults.getMetalAlloyRegion(i,j);
                        region.copyRegionColors(metalAlloyRegions[i][j]);
                    }
                }
                break;

            case LEFT:
                quadrantHeight = 2 * (height / 3);
                quadrantWidth = width / 3;
                for (int i = height / 3; i < quadrantHeight; i++) {
                    for (int j = 0; j < quadrantWidth; j++) {
                        double tempOfRegion = metalAlloyRegions[i][j].getTemperature();
                        alloyToStoreResults.setTempOfRegion(tempOfRegion, i, j);
                        MetalAlloyRegion region = alloyToStoreResults.getMetalAlloyRegion(i,j);
                        region.copyRegionColors(metalAlloyRegions[i][j]);
                    }
                }
                break;

            case MIDDLE:
                quadrantHeight = 2 * (height / 3);
                quadrantWidth = 2 * (width / 3);
                for (int i = height / 3; i < quadrantHeight; i++) {
                    for (int j = width / 3; j < quadrantWidth; j++) {
                        double tempOfRegion = metalAlloyRegions[i][j].getTemperature();
                        alloyToStoreResults.setTempOfRegion(tempOfRegion, i, j);
                        MetalAlloyRegion region = alloyToStoreResults.getMetalAlloyRegion(i,j);
                        region.copyRegionColors(metalAlloyRegions[i][j]);
                    }
                }
                break;

            case RIGHT:
                quadrantHeight = 2 * (height / 3);
                quadrantWidth = width;
                for (int i = height / 3; i < quadrantHeight; i++) {
                    for (int j = 2 * (width / 3); j < quadrantWidth; j++) {
                        double tempOfRegion = metalAlloyRegions[i][j].getTemperature();
                        alloyToStoreResults.setTempOfRegion(tempOfRegion, i, j);
                        MetalAlloyRegion region = alloyToStoreResults.getMetalAlloyRegion(i,j);
                        region.copyRegionColors(metalAlloyRegions[i][j]);
                    }
                }
                break;

            case BOTTOM_LEFT:
                quadrantHeight = height;
                quadrantWidth = width / 3;
                for (int i = 2 * (height / 3); i < quadrantHeight; i++) {
                    for (int j = 0; j < quadrantWidth; j++) {
                        double tempOfRegion = metalAlloyRegions[i][j].getTemperature();
                        alloyToStoreResults.setTempOfRegion(tempOfRegion, i, j);
                        MetalAlloyRegion region = alloyToStoreResults.getMetalAlloyRegion(i,j);
                        region.copyRegionColors(metalAlloyRegions[i][j]);
                    }
                }
                break;

            case BOTTOM:
                quadrantHeight = height;
                quadrantWidth = 2 * (width / 3);
                for (int i = 2 * (height / 3); i < quadrantHeight; i++) {
                    for (int j = width / 3; j < quadrantWidth; j++) {
                        double tempOfRegion = metalAlloyRegions[i][j].getTemperature();
                        alloyToStoreResults.setTempOfRegion(tempOfRegion, i, j);
                        MetalAlloyRegion region = alloyToStoreResults.getMetalAlloyRegion(i,j);
                        region.copyRegionColors(metalAlloyRegions[i][j]);
                    }
                }
                break;

            case BOTTOM_RIGHT:
                quadrantHeight = height;
                quadrantWidth = width;
                for (int i = 2 * (height / 3); i < quadrantHeight; i++) {
                    for (int j = 2 * (width / 3); j < quadrantWidth; j++) {
                        double tempOfRegion = metalAlloyRegions[i][j].getTemperature();
                        alloyToStoreResults.setTempOfRegion(tempOfRegion, i, j);
                        MetalAlloyRegion region = alloyToStoreResults.getMetalAlloyRegion(i,j);
                        region.copyRegionColors(metalAlloyRegions[i][j]);
                    }
                }
                break;
        }
    }

    public void deepCopyRegionsTo(MetalAlloy alloyToStore) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double tempOfRegion = metalAlloyRegions[i][j].getTemperature();
                alloyToStore.setTempOfRegion(tempOfRegion, i, j);
                MetalAlloyRegion region = alloyToStore.getMetalAlloyRegion(i,j);
                region.deepCopyRegion(metalAlloyRegions[i][j]);
            }
        }
    }
}
