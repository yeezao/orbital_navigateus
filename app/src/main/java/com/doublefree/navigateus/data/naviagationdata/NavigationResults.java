package com.doublefree.navigateus.data.naviagationdata;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class NavigationResults implements Parcelable {

    private NavigationPartialResults firstRunResult;
    private int totalTimeTaken = 0;
    private List<NavigationPartialResults> resultsConcatenated;
    private boolean showResult = true;
    private int displayTotalTimeTaken = 0;

    public NavigationResults() {
    }

    protected NavigationResults(Parcel in) {
        firstRunResult = in.readParcelable(NavigationPartialResults.class.getClassLoader());
        totalTimeTaken = in.readInt();
        resultsConcatenated = in.createTypedArrayList(NavigationPartialResults.CREATOR);
        showResult = in.readByte() != 0;
        displayTotalTimeTaken = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(firstRunResult, flags);
        dest.writeInt(totalTimeTaken);
        dest.writeTypedList(resultsConcatenated);
        dest.writeByte((byte) (showResult ? 1 : 0));
        dest.writeInt(displayTotalTimeTaken);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NavigationResults> CREATOR = new Creator<NavigationResults>() {
        @Override
        public NavigationResults createFromParcel(Parcel in) {
            return new NavigationResults(in);
        }

        @Override
        public NavigationResults[] newArray(int size) {
            return new NavigationResults[size];
        }
    };

    public boolean isShowResult() {
        return showResult;
    }

    public void setShowResult(boolean showResult) {
        this.showResult = showResult;
    }

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

    public NavigationPartialResults getFirstRunResult() {
        return firstRunResult;
    }

    public void setFirstRunResult(NavigationPartialResults firstRunResult) {
        this.firstRunResult = firstRunResult;
    }

    public int getDisplayTotalTimeTaken() {
        return displayTotalTimeTaken;
    }

    public void setDisplayTotalTimeTaken(int displayTotalTimeTaken) {
        this.displayTotalTimeTaken = displayTotalTimeTaken;
    }


    public NavigationResults clone() throws CloneNotSupportedException {
        NavigationResults navigationResultsClone = (NavigationResults) super.clone();
        return navigationResultsClone;
    }
}
