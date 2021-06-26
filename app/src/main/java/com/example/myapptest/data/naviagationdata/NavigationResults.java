package com.example.myapptest.data.naviagationdata;

import java.util.List;

public class NavigationResults {

    private int totalTimeTaken = 0;
    private List<NavigationPartialResults> resultsConcatenated;

    public int getTotalTimeTaken() {
        return totalTimeTaken;
    }

    public void setTotalTimeTaken(int totalTimeTaken) {
        this.totalTimeTaken = totalTimeTaken;
    }

    public List<NavigationPartialResults> getResultsConcatenated() {
        return resultsConcatenated;
    }

    public void setResultsConcatenated(List<NavigationPartialResults> resultsConcatenated) {
        this.resultsConcatenated = resultsConcatenated;
    }

    public void addResultsConcatenated(NavigationPartialResults resultsConcatenated) {
        this.resultsConcatenated.add(resultsConcatenated);
    }


}
