package com.vlad.Model;

import java.util.List;

public class GeneratorContext{
    private PseudoRandomGenerator strategy;
    private List<IterationState> history;
    private GenerationMode currentMode;

    GeneratorContext(PseudoRandomGenerator strategy){
        this.strategy = strategy;
    }

    public void setStrategy(PseudoRandomGenerator strategy, GenerationMode mode) {
        this.strategy = strategy;
        this.currentMode = mode;
    }

    public List<IterationState> getHistory() {
        return history;
    }

    public void clearHistory(){
        history.clear();
    }

    public void setCurrentMode(GenerationMode currentMode) {
        this.currentMode = currentMode;
    }

    public void startGeneration(long count){
        for(long i = 0; i < count; i++){
            performSingleStep();
        }
    }

    public void performSingleStep(){
        IterationState result =  strategy.generateNextStep();
        history.add(result);
    }
}
