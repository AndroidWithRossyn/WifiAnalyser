package com.example.wifianalyser.roomdb

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AccessPointDao {

    @Insert
    suspend fun insertAccessPointsInDB(accessPointsList: List<AccessPointsModel>)

    @Query("Select * FROM AccessPointsTable ORDER BY col_dbm DESC")
    fun getAccessPointsFromDB() : LiveData<List<AccessPointsModel>>

    @Query("Delete FROM AccessPointsTable")
    suspend fun deleteAccessPointsFromDB()

    @Transaction
    suspend fun updateData(accessPoints: ArrayList<AccessPointsModel>) {
        deleteAccessPointsFromDB()
        insertAccessPointsInDB(accessPoints)
    }
}