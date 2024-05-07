package com.example.simondice

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "HighscoreDB"
        private const val TABLE_HIGHSCORE = "highscore"
        private const val KEY_ID = "id"
        private const val KEY_SCORE = "score"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_HIGHSCORE($KEY_ID INTEGER PRIMARY KEY, $KEY_SCORE INTEGER)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HIGHSCORE")
        onCreate(db)
    }

    fun addHighscore(score: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_SCORE, score)
        db.insert(TABLE_HIGHSCORE, null, values)
        db.close()
    }

    fun getHighscore(): Int {
        val db = this.readableDatabase
        var highscore = 0
        val query = "SELECT * FROM $TABLE_HIGHSCORE ORDER BY $KEY_SCORE DESC LIMIT 1"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val scoreIndex = cursor.getColumnIndex(KEY_SCORE)
            highscore = cursor.getInt(scoreIndex)
        }
        cursor.close()
        db.close()
        return highscore
    }

    init {
        // Agregar un highscore inicial si la tabla está vacía
        if (getHighscore() == 0) {
            addHighscore(0)
        }
    }
}