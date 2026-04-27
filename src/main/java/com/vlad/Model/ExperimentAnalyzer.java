package com.vlad.Model;


public class ExperimentAnalyzer {
    public double runPerformanceExperiment(PseudoRandomGenerator generator, int iterations){
        double startTime = System.nanoTime();

        GeneratorContext generatorContext = new GeneratorContext(generator);
        generatorContext.startGeneration(iterations);

        double endTime = System.nanoTime();

        return endTime - startTime;
    }
}
