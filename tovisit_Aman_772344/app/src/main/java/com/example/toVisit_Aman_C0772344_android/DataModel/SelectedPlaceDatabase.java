package com.example.toVisit_Aman_C0772344_android.DataModel;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.toVisit_Aman_C0772344_android.SelectedPlaceData;

@Database(entities = {SelectedPlaceData.class}, version = 1)
public abstract class SelectedPlaceDatabase extends RoomDatabase {
    private static final String DB_NAME = "place_database";

    public abstract SelectedPlaceDataDao favouriteDataDao();

    private static SelectedPlaceDatabase favouritePlaceDb;

    public static SelectedPlaceDatabase getInstance(Context context) {
        if (null == favouritePlaceDb) {
            favouritePlaceDb = buildDatabaseInstance(context);
        }
        return favouritePlaceDb;
    }

    private static SelectedPlaceDatabase buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(context,
                SelectedPlaceDatabase.class,
                DB_NAME)
                .allowMainThreadQueries().build();
    }

    public void cleanUp() {
        favouritePlaceDb = null;
    }
}

