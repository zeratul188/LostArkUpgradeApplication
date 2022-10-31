package com.example.lostarkupgradeapplication.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.lostarkupgradeapplication.upgrade.objects.Tierup
import java.sql.SQLException

class TierupDBAdapter {
    val tag = "TierupDBAdapter"
    val table_name = "tierup"

    var context: Context
    lateinit var db: SQLiteDatabase
    var loadDatabase: LoadDatabase

    constructor(context: Context) {
        this.context = context
        loadDatabase = LoadDatabase("tierup", context)
    }

    fun open(): TierupDBAdapter {
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

    fun getItem(statue: Int, before: Int): Tierup? {
        var tierup: Tierup? = null
        try {
            val sql = "SELECT * FROM $table_name"

            val cursor: Cursor = db.rawQuery(sql, null)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (statue == cursor.getInt(1) && before == cursor.getInt(2)) {
                        tierup = Tierup(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3))
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return tierup
    }
}