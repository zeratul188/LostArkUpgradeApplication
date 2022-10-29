package com.example.lostarkupgradeapplication.room

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Material::class], version = 1)
abstract class MaterialDatabase: RoomDatabase() {
    abstract fun materialDao(): MaterialDao

    companion object {
        private var instance: MaterialDatabase? = null

        @Synchronized
        fun getInstance(context: Context): MaterialDatabase? {
            if (instance == null) {

            }
            return instance
        }
    }
}