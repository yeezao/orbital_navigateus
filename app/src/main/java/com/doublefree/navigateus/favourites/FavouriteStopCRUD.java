package com.doublefree.navigateus.favourites;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FavouriteStopCRUD {

        @Insert
        public void addData(FavouriteStop favouriteStop);

        @Query("select * from favouriteStops")
        public List<FavouriteStop> getFavoriteData();

        @Query("select * from favouriteStops WHERE stopId=:stopId")
        public FavouriteStop getFavoriteDataSingle(String stopId);

        @Query("SELECT EXISTS (SELECT 1 FROM favouriteStops WHERE stopId=:stopId)")
        public int isFavorite(String stopId);

        @Update
        public int updateData(FavouriteStop favouriteStop);

        @Delete
        public void delete(FavouriteStop d);


}
