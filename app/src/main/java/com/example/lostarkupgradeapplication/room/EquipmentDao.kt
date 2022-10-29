package com.example.lostarkupgradeapplication.room

import androidx.room.*

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment")
    fun getAll(): List<Equipment>

    @Query("SELECT * FROM equipment WHERE type LIKE :type")
    fun findByType(type: String): Equipment

    @Insert
    fun insertAll(vararg equipment: Equipment)

    @Delete
    fun delete(equipment: Equipment)

    @Update
    fun update(equipment: Equipment)
}