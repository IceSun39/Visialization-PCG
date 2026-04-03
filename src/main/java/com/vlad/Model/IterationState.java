package com.vlad.Model;

public class IterationState {
    private final double xValue;
    private final double yValue;
    private final int iterationNumber;

    public IterationState(double xValue, double yValue, int iterationNumber) {
        this.xValue = xValue;
        this.yValue = yValue;
        this.iterationNumber = iterationNumber;
    }

    public double getXValue() {
        return xValue;
    }

    public double getYValue() {
        return yValue;
    }

    public int getIterationNumber() {
        return iterationNumber;
    }

    public boolean isDataValid() {
        return xValue >= 0.0 && xValue < 1.0 && yValue >= 0.0 && yValue < 1.0;
    }
}