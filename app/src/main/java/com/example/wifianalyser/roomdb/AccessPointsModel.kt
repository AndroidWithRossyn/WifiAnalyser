package com.example.wifianalyser.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AccessPointsTable")
data class AccessPointsModel(

    @ColumnInfo(name = "col_name")
    val accessPointName: String = "",

    @ColumnInfo(name = "col_dbm")
    val dbm: Int = -1,

    @PrimaryKey
    @ColumnInfo(name = "col_MacAddress")
    val MacAddress: String = "",

    @ColumnInfo(name = "col_channel")
    val channel: Int = -1,

    @ColumnInfo(name = "col_frequency")
    val frequency: Int = -1,

    @ColumnInfo(name = "security")
    val security : String = ""
)





