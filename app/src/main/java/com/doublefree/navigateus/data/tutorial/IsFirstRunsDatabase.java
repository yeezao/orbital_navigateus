package com.doublefree.navigateus.data.tutorial;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities={IsFirstRuns.class},version = 1, exportSchema = false)
public abstract class IsFirstRunsDatabase extends RoomDatabase {

    public abstract IsFirstRunsCRUD isFirstRunsCRUD();


}
