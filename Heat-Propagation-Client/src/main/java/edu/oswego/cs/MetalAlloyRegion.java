package edu.oswego.cs;

import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MetalAlloyRegion implements Serializable {

    private double temperature;
    private volatile int r;
    private volatile int g;
    private volatile int b;
    public static final double TEMPTHRESHOLD1 = 0;
    public static final int RTHRESHOLD1 = 13;
    public static final int GTHRESHOLD1 = 21;
    public static final int BTHRESHOLD1 = 235;
    public static final double TEMPTHRESHOLD2 = 400;
    public static final int RTHRESHOLD2 = 253;
    public static final int GTHRESHOLD2 = 29;
    public static final int BTHRESHOLD2 = 29;
    public static final double TEMPTHRESHOLD3 = 1500;
    public static final int RTHRESHOLD3 = 247;
    public static final int GTHRESHOLD3 = 250;
    public static final int BTHRESHOLD3 = 143;
    private double percentOfMetal1;
    private double percentOfMetal2;
    private double percentOfMetal3;


    public MetalAlloyRegion(double temperature) {
        this.temperature = temperature;
        Random random = ThreadLocalRandom.current();
        double baseMetalPercent = 1.0 / 3.0;
        // varying metal composition
        double variance = baseMetalPercent * 0.25;
        percentOfMetal3 = baseMetalPercent + random.nextDouble(-variance, variance);
        percentOfMetal1 = baseMetalPercent + random.nextDouble(-variance, variance);;
        percentOfMetal2 = baseMetalPercent + random.nextDouble(-variance, variance);;

        // shift the random values to add up to 100%
        double totalPercentAmount = percentOfMetal1 + percentOfMetal2 + percentOfMetal3;
        double differenceBetweenRequiredPercentAndCurrentTotal = totalPercentAmount - 1.0;

        // Adjust difference to move in a favorable direction, if our difference is negative we know that we must increase the overall percentage and so on.
        double partialDifferenceForAdjustment = -differenceBetweenRequiredPercentAndCurrentTotal / 3.0;
        // find the amount of metals that vary below the average
        int amountBelowAverage = 0;
        if (percentOfMetal3 < baseMetalPercent) amountBelowAverage++;
        if (percentOfMetal2 < baseMetalPercent) amountBelowAverage++;
        if (percentOfMetal1 < baseMetalPercent) amountBelowAverage++;

        if (differenceBetweenRequiredPercentAndCurrentTotal < 0) { // We need to increase the overall percentage
            // adjustment for percents above the baseline
            double aboveBaseLineAdjustment = partialDifferenceForAdjustment / 2.0;
            int amountAboveAverage = 3 - amountBelowAverage;
            double belowBaseLineAdjustment = partialDifferenceForAdjustment + (amountAboveAverage * amountAboveAverage * (0.75 * partialDifferenceForAdjustment));
            if (percentOfMetal3 < baseMetalPercent) {
                percentOfMetal3 += belowBaseLineAdjustment;
            } else {
                percentOfMetal3 -= aboveBaseLineAdjustment;
            }
            if (percentOfMetal2 < baseMetalPercent) {
                percentOfMetal2 += belowBaseLineAdjustment;
            } else {
                percentOfMetal2 -= aboveBaseLineAdjustment;
            }
            if (percentOfMetal1 < baseMetalPercent) {
                percentOfMetal1 += belowBaseLineAdjustment;
            } else {
                percentOfMetal1 -= aboveBaseLineAdjustment;
            }
        } else if (differenceBetweenRequiredPercentAndCurrentTotal > 0) { // we need to decrease the overall percentage
            // adjustment for percents above the baseline
            double aboveBaseLineAdjustment = partialDifferenceForAdjustment + (amountBelowAverage * amountBelowAverage * ( 0.75 * partialDifferenceForAdjustment));
            double belowBaseLineAdjustment = partialDifferenceForAdjustment / 2.0;
            if (percentOfMetal3 < baseMetalPercent) {
                percentOfMetal3 -= belowBaseLineAdjustment;
            } else {
                percentOfMetal3 += aboveBaseLineAdjustment;
            }
            if (percentOfMetal2 < baseMetalPercent) {
                percentOfMetal2 -= belowBaseLineAdjustment;
            } else {
                percentOfMetal2 += aboveBaseLineAdjustment;
            }
            if (percentOfMetal1 < baseMetalPercent) {
                percentOfMetal1 -= belowBaseLineAdjustment;
            } else {
                percentOfMetal1 += aboveBaseLineAdjustment;
            }
        }
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getTemperature() {
        return temperature;
    }

    public void increaseTemperature(double tempIncrease) {
        temperature += tempIncrease;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public double getPercentOfMetal1() {
        return percentOfMetal1;
    }

    public double getPercentOfMetal2() {
        return percentOfMetal2;
    }

    public double getPercentOfMetal3() {
        return percentOfMetal3;
    }

    /**
     * Calculates the current r, g, and b values of the metal alloy region.
     */
    public void calcRGB() {
        if (temperature <= TEMPTHRESHOLD1) {
            r = RTHRESHOLD1;
            g = GTHRESHOLD1;
            b = BTHRESHOLD1;
        } else if (temperature < TEMPTHRESHOLD2) {
            r = calcColorBetweenThreshold(temperature, RTHRESHOLD1, RTHRESHOLD2, TEMPTHRESHOLD1, TEMPTHRESHOLD2);
            g = calcColorBetweenThreshold(temperature, GTHRESHOLD1, GTHRESHOLD2, TEMPTHRESHOLD1, TEMPTHRESHOLD2);
            b = calcColorBetweenThreshold(temperature, BTHRESHOLD1, BTHRESHOLD2, TEMPTHRESHOLD1, TEMPTHRESHOLD2);
        } else if (temperature < TEMPTHRESHOLD3) {
            r = calcColorBetweenThreshold(temperature, RTHRESHOLD2, RTHRESHOLD3, TEMPTHRESHOLD2, TEMPTHRESHOLD3);
            g = calcColorBetweenThreshold(temperature, GTHRESHOLD2, GTHRESHOLD3, TEMPTHRESHOLD2, TEMPTHRESHOLD3);
            b = calcColorBetweenThreshold(temperature, BTHRESHOLD2, BTHRESHOLD3, TEMPTHRESHOLD2, TEMPTHRESHOLD3);
        } else {
            r = RTHRESHOLD3;
            g = GTHRESHOLD3;
            b = BTHRESHOLD3;
        }
    }

    /**
     * Calculates the color value based on the temperature of the alloy region, the starting color threshold, the difference
     * between color thresholds, and the temperature range of the color difference.
     * @param temperature the temperature of the region
     * @param colorThreshold1 the color values starting point
     * @param colorThreshold2 the color values ending point
     * @param tempThreshold1 the temperature ranges starting point
     * @param tempThreshold2 the temperature ranges ending point
     * @return The calculated color value for a rgb value of the alloy.
     */
    public int calcColorBetweenThreshold(double temperature, int colorThreshold1, int colorThreshold2, double tempThreshold1, double tempThreshold2) {
        return (int)Math.floor(colorThreshold1 + (temperature * (colorThreshold2 - colorThreshold1) / (tempThreshold2)));
    }

    public void copyRegionColors(MetalAlloyRegion regionToCopy) {
        r = regionToCopy.getR();
        g = regionToCopy.getG();
        b = regionToCopy.getB();
    }

    public void deepCopyRegion(MetalAlloyRegion regionToCopy) {
        r = regionToCopy.getR();
        g = regionToCopy.getG();
        b = regionToCopy.getB();
        percentOfMetal1 = regionToCopy.getPercentOfMetal1();
        percentOfMetal2 = regionToCopy.getPercentOfMetal2();
        percentOfMetal3 = regionToCopy.getPercentOfMetal3();
    }
}
