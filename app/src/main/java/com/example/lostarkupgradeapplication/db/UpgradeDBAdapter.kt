package com.example.lostarkupgradeapplication.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.lostarkupgradeapplication.upgrade.objects.Upgrade
import java.sql.SQLException

class UpgradeDBAdapter {
    val tag = "UpgradeDBAdapter"
    val table_name = "upgrades"

    var context: Context
    lateinit var db: SQLiteDatabase
    var loadDatabase: LoadDatabase

    constructor(context: Context) {
        this.context = context
        loadDatabase = LoadDatabase("upgrades", context)
    }

    fun open(): UpgradeDBAdapter {
        try {
            loadDatabase.open()
            loadDatabase.close()
            db = loadDatabase.readableDatabase
        } catch (e: SQLException) {
            e.printStackTrace()
            Log.d("Error(SQLException)", "Error Content : $e")
        }
        return this
    }

    fun close() {
        loadDatabase.close()
    }

    fun getItem(type: String, step: Int): Upgrade? {
        var upgrade: Upgrade? = null
        try {
            val sql = "SELECT * FROM $table_name"

            val cursor: Cursor = db.rawQuery(sql, null)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    upgrade = Upgrade(
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getDouble(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getInt(8),
                        cursor.getInt(9),
                        cursor.getInt(10),
                        cursor.getInt(11),
                        cursor.getDouble(12),
                        cursor.getDouble(13),
                        cursor.getDouble(14),
                        cursor.getInt(15),
                        cursor.getInt(16),
                        cursor.getInt(17),
                        cursor.getInt(18)
                    )
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return upgrade
    }
}