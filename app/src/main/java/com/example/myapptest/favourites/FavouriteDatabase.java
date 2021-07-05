package com.example.myapptest.favourites;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities={FavouriteStop.class},version = 1, exportSchema = false)
public abstract class FavouriteDatabase extends RoomDatabase {

    public abstract FavouriteStopCRUD favouriteStopCRUD();


}
