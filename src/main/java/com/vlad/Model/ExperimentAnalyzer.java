package com.vlad.Model;


public class ExperimentAnalyzer {
    public double runPerformanceExperiment(PseudoRandomGenerator generator, long iterations){
        double startTime = System.nanoTime();

        for(long i = 0; i < iterations; i++){
            generator.nextInt();
        }

        double endTime = System.nanoTime();

        return endTime - startTime;
    }
}
