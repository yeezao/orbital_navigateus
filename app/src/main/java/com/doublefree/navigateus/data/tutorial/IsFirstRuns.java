package com.doublefree.navigateus.data.tutorial;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="firstRunData")
public class IsFirstRuns {

    @PrimaryKey
    @NonNull
    private int key = 0;

    @ColumnInfo(name = "firstRunHomeFrag")
    private boolean firstRunHomeFrag = true;

    @ColumnInfo(name = "firstRunStopsFrag")
    private boolean firstRunStopsFrag = true;

    @ColumnInfo(name = "firstRunServicesFrag")
    private boolean firstRunServicesFrag = true;

    @ColumnInfo(name = "firstRunSingleServiceFrag")
    private boolean firstRunSingleServiceFrag = true;

    @ColumnInfo(name = "firstRunNavFrag")
    private boolean firstRunNavFrag = true;

    @ColumnInfo(name = "firstRunNavResultFrag")
    private boolean firstRunNavResultFrag = true;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isFirstRunHomeFrag() {
        return firstRunHomeFrag;
    }

    public void setFirstRunHomeFrag(boolean firstRunHomeFrag) {
        this.firstRunHomeFrag = firstRunHomeFrag;
    }

    public boolean isFirstRunStopsFrag() {
        return firstRunStopsFrag;
    }

    public void setFirstRunStopsFrag(boolean firstRunStopsFrag) {
        this.firstRunStopsFrag = firstRunStopsFrag;
    }

    public boolean isFirstRunServicesFrag() {
        return firstRunServicesFrag;
    }

    public void setFirstRunServicesFrag(boolean firstRunServicesFrag) {
        this.firstRunServicesFrag = firstRunServicesFrag;
    }

    public boolean isFirstRunSingleServiceFrag() {
        return firstRunSingleServiceFrag;
    }

    public void setFirstRunSingleServiceFrag(boolean firstRunSingleServiceFrag) {
        this.firstRunSingleServiceFrag = firstRunSingleServiceFrag;
    }

    public boolean isFirstRunNavFrag() {
        return firstRunNavFrag;
    }

    public void setFirstRunNavFrag(boolean firstRunNavFrag) {
        this.firstRunNavFrag = firstRunNavFrag;
    }

    public boolean isFirstRunNavResultFrag() {
        return firstRunNavResultFrag;
    }

    public void setFirstRunNavResultFrag(boolean firstRunNavResultFrag) {
        this.firstRunNavResultFrag = firstRunNavResultFrag;
    }



}
