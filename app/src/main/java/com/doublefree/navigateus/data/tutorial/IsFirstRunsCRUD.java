package com.doublefree.navigateus.data.tutorial;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface IsFirstRunsCRUD {

        @Insert
        public void addData(IsFirstRuns isFirstRuns);

        @Query("select * from firstRunData")
        public List<IsFirstRuns> getIsFirstRuns();

//        @Query("select * from favouriteStops WHERE stopId=:stopId")
//        public I getFavoriteDataSingle(String stopId);

        @Query("SELECT EXISTS (SELECT 1 FROM firstRunData)")
        public int isFirstRunPresent();

        @Update
        public int updateData(IsFirstRuns isFirstRuns);

        @Delete
        public void delete(IsFirstRuns isFirstRuns);


}
