package com.example.myapptest.data.naviagationdata;

import java.util.ArrayList;
import java.util.List;

public class NavigationPartialResults {

    private int timeForSegment;
    private int timeAtEndOfSegment = 0;
    private List<NavigationNodes> nodesTraversed = new ArrayList<>();
    private String[] busToWaitFor = new String[2];
    private List<String> viableBuses1 = new ArrayList<>();
    private List<String> viableBuses2 = new ArrayList<>();
    private int busWaitingTime = 0;
    private NavigationNodes transferStop;
    private List<NavigationNodes> beforeTransferNodesTraversed = new ArrayList<>();
    private List<NavigationNodes> afterTransferNodesTraversed = new ArrayList<>();
    private List<NavigationEdges> edgeSequence = new ArrayList<>();
    private List<NavigationNodes> nodeSequence = new ArrayList<>();

    public int getTimeForSegment() {
        return timeForSegment;
    }

    public void setTimeForSegment(int timeForSegment) {
        this.timeForSegment = timeForSegment;
    }

    public int getTimeAtEndOfSegment() {
        return timeAtEndOfSegment;
    }

    public void setTimeAtEndOfSegment(int timeAtEndOfSegment) {
        this.timeAtEndOfSegment = timeAtEndOfSegment;
    }

    public List<NavigationNodes> getNodesTraversed() {
        return nodesTraversed;
    }

    public void setNodesTraversed(List<NavigationNodes> nodesTraversed) {
        this.nodesTraversed = nodesTraversed;
    }

    public String[] getBusToWaitFor() {
        return busToWaitFor;
    }

    public void setBusToWaitFor(String[] busToWaitFor) {
        this.busToWaitFor = busToWaitFor;
    }

    public void setBusToWaitForIndiv(String busToWaitFor, int index) {
        this.busToWaitFor[index] = busToWaitFor;
    }

    public int getBusWaitingTime() {
        return busWaitingTime;
    }

    public void setBusWaitingTime(int busWaitingTime1) {
        this.busWaitingTime = busWaitingTime1;
    }

    public NavigationNodes getTransferStop() {
        return transferStop;
    }

    public void setTransferStop(NavigationNodes transferStop) {
        this.transferStop = transferStop;
    }

    public List<NavigationNodes> getBeforeTransferNodesTraversed() {
        return beforeTransferNodesTraversed;
    }

    public void setBeforeTransferNodesTraversed(List<NavigationNodes> beforeTransferNodesTraversed) {
        this.beforeTransferNodesTraversed = beforeTransferNodesTraversed;
    }

    public void setBeforeTransferNodesTraversedIndiv(NavigationNodes beforeTransferNodesTraversedIndiv) {
        this.beforeTransferNodesTraversed.add(beforeTransferNodesTraversedIndiv);
    }

    public List<NavigationNodes> getAfterTransferNodesTraversed() {
        return afterTransferNodesTraversed;
    }

    public void setAfterTransferNodesTraversed(List<NavigationNodes> afterTransferNodesTraversed) {
        this.afterTransferNodesTraversed = afterTransferNodesTraversed;
    }

    public void setAfterTransferNodesTraversedIndiv(NavigationNodes afterTransferNodesTraversedIndiv) {
        this.afterTransferNodesTraversed.add(afterTransferNodesTraversedIndiv);
    }


    public List<String> getViableBuses1() {
        return viableBuses1;
    }

    public void setViableBuses1(List<String> viableBuses1) {
        this.viableBuses1 = viableBuses1;
    }

    public void addViableBuses1(String viableBus1) {
        this.viableBuses1.add(viableBus1);
    }

    public List<String> getViableBuses2() {
        return viableBuses2;
    }

    public void setViableBuses2(List<String> viableBuses2) {
        this.viableBuses2 = viableBuses2;
    }

    public void addViableBuses2(String viableBus2) {
        this.viableBuses2.add(viableBus2);
    }

    public List<NavigationEdges> getEdgeSequence() {
        return edgeSequence;
    }

    public void setEdgeSequence(List<NavigationEdges> edgeSequence) {
        this.edgeSequence = edgeSequence;
    }

    public void addEdgeSequence(NavigationEdges singleEdge) {
        this.edgeSequence.add(singleEdge);
    }

    public List<NavigationNodes> getNodeSequence() {
        return nodeSequence;
    }

    public void setNodeSequence(List<NavigationNodes> nodeSequence) {
        this.nodeSequence = nodeSequence;
    }



}
