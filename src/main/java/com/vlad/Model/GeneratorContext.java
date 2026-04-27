package com.vlad.Model;

import java.util.ArrayList;
import java.util.List;

public class GeneratorContext {
    private PseudoRandomGenerator strategy;
    private List<IterationState> history = new ArrayList<>();

    public GeneratorContext(PseudoRandomGenerator strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(PseudoRandomGenerator strategy) {
        this.strategy = strategy;
    }

    public List<IterationState> getHistory() {
        return history;
    }

    public void clearHistory() {
        if (history != null) {
            history.clear();
        }
    }

    public void startGeneration(long count) {
        for (long i = 0; i < count; i++) {
            performSingleStep();
        }
    }

    public void performSingleStep() {
        IterationState result = strategy.generateNextStep();
        history.add(result);
    }
}