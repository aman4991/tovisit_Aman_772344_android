package com.example.toVisit_Aman_C0772344_android.DataModel;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.toVisit_Aman_C0772344_android.SelectedPlaceData;

import java.util.List;

@Dao
public interface SelectedPlaceDataDao {
    @Query("Select * from SelectedPlaceData")
    List<SelectedPlaceData> getFavouriteList();

    @Insert
    void insertFavouritePlaceData(SelectedPlaceData selectedPlaceData);

    @Update
    void updateFavouriteData(SelectedPlaceData selectedPlaceData);

    @Delete
    void deleteFavouritePlace(SelectedPlaceData selectedPlaceData);
}
