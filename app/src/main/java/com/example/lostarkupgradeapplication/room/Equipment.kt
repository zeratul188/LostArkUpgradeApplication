package com.example.lostarkupgradeapplication.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Equipment (
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "type") var type: String?,
    @ColumnInfo(name = "level") var level: Int?,
    @ColumnInfo(name = "itemlevel") var itemlevel: Int?,
    @ColumnInfo(name = "power") var power: Double?,
    @ColumnInfo(name = "honer") var honer: Int?,
    @ColumnInfo(name = "statue") var statue: Int?,
    @ColumnInfo(name = "stack") var stack: Int?
) {
    fun getProgress(): Int {
        return power?.times(100)?.toInt() ?: 0
    }
}
