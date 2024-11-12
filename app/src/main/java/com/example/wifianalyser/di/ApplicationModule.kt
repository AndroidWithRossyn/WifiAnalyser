package com.example.wifianalyser.di

import android.content.Context
import android.net.wifi.WifiManager
import androidx.room.Room
import com.example.wifianalyser.roomdb.AccessPointDao
import com.example.wifianalyser.roomdb.MyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApplicationModule {

    @Provides
    @Singleton
    fun getDatabase(@ApplicationContext context: Context): MyDatabase {
        return Room.databaseBuilder(context, MyDatabase::class.java, "wifi-analyzer").build()
    }

    @Provides
    @Singleton
    fun getWifiDao(database: MyDatabase): AccessPointDao {
        return database.accessPointsDAO()
    }

    @Provides
    @Singleton
    fun getWifiManager(@ApplicationContext context: Context) : WifiManager{
        return context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    @Provides
    fun getContext(@ApplicationContext context: Context) : Context{
        return  context
    }
    }


