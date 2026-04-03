package com.vlad.Model;

public interface PseudoRandomGenerator {
    int nextInt();
    int nextInt(int bound);
    double nextDouble();

    IterationState generateNextStep();

    void setSeed(long seed);
}
