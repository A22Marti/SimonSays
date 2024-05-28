package com.example.simondice
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "videoDatabase3"
        private const val TABLE_HIGHSCORE = "highscore"
        private const val KEY_ID = "id"
        private const val KEY_SCORE = "score"
        private const val KEY_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_HIGHSCORE($KEY_ID INTEGER PRIMARY KEY, $KEY_SCORE INTEGER, $KEY_DATE TEXT)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HIGHSCORE")
        onCreate(db)
    }

    fun addHighscore(score: Int, date: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_SCORE, score)
        values.put(KEY_DATE, date)
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

    fun getHighscoreOfToday(): Int {
        val db = this.readableDatabase
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var highscore = 0
        val query = "SELECT MAX($KEY_SCORE) FROM $TABLE_HIGHSCORE WHERE $KEY_DATE = ?"
        val cursor = db.rawQuery(query, arrayOf(currentDate))
        if (cursor.moveToFirst()) {
            highscore = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return highscore
    }

    init {
        if (getHighscore() == 0) {
            addHighscore(0, "")
        }
    }
}
