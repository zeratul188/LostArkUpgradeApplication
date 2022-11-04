package com.example.lostarkupgradeapplication.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lostarkupgradeapplication.App
import com.example.lostarkupgradeapplication.R

@Entity
data class Material(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "tier") var tier: Int,
    @ColumnInfo(name = "count") var count: Int
)
