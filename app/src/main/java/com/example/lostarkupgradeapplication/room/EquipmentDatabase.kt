package com.example.lostarkupgradeapplication.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Equipment::class], version = 1)
abstract class EquipmentDatabase: RoomDatabase() {
    abstract fun equipmentDao(): EquipmentDao

    companion object {
        private var instance: EquipmentDatabase? = null

        //싱글톤 사용
        @Synchronized
        fun getInstance(context: Context): EquipmentDatabase? {
            if (instance == null) {
                synchronized(EquipmentDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        EquipmentDatabase::class.java,
                        "equipment"
                    ).build()
                }
            }
            return instance
        }
    }
}