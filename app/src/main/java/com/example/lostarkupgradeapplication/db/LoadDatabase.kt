package com.example.lostarkupgradeapplication.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.lostarkupgradeapplication.App
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class LoadDatabase: SQLiteOpenHelper {
    var name = "null"
    var context: Context
    val root = App.context().packageName+"/databases/"

    lateinit var mDatabase: SQLiteDatabase

    constructor(name: String, context: Context): super(context, "${name}.db", null, 1) { //version check
        this.name = name
        this.context = context
        databaseCheck()
    }

    fun databaseCheck() {
        val dbFile = File("${root}${name}.db")
        dbCopy()
    }

    fun dbCopy() {
        try {
            val folder = File(root)
            if (!folder.exists()) {
                folder.mkdir()
            }
            val inputStream: InputStream = context.assets.open("${name}.db")
            val filepath = "${root}${name}.db"
            val outputStream: OutputStream = FileOutputStream(filepath)
            val buffer = ByteArray(1024)
            var length: Int = inputStream.read(buffer)
            while (length > 0) {
                outputStream.write(buffer, 0, length)
                length = inputStream.read(buffer)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("Error(IOException)", "Error Content : $e")
        }
    }

    fun open(): Boolean {
        val path = "${root}${name}.db"
        mDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY)
        return mDatabase != null
    }

    override fun close() {
        if (mDatabase != null) {
            mDatabase.close()
        }
        super.close()
    }

    override fun onCreate(p0: SQLiteDatabase?) {

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }
}