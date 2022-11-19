package com.example.lostarkupgradeapplication.room

import androidx.room.*

@Dao
interface MaterialDao {
    @Query("SELECT * FROM material")
    fun getAll(): List<Material>

    @Query("SELECT * FROM material WHERE type LIKE :type AND tier LIKE :tier")
    fun findItem(type: String, tier: Int): Material

    @Insert
    fun insertAll(vararg Material: Material)

    @Delete
    fun delete(Material: Material)

    @Update
    fun update(Material: Material)
}