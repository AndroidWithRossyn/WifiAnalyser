package com.example.wifianalyser.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AccessPointsModel::class], version = 2, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {
    abstract fun accessPointsDAO(): AccessPointDao
}