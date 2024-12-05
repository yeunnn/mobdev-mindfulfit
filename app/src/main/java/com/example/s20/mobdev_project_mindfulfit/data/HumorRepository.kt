package com.example.s20.mobdev_project_mindfulfit.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class HumorRepository(private val dbHelper: DatabaseHelper) {

    fun saveJoke(joke: String, date: String) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("joke", joke)
            put("date", date)
            put("streak", getStreak() + 1)
        }
        db.insertWithOnConflict("jokes", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getJoke(date: String): Pair<String, Int>? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT joke, streak FROM jokes WHERE date = ?", arrayOf(date))
        return if (cursor.moveToFirst()) {
            val joke = cursor.getString(cursor.getColumnIndexOrThrow("joke"))
            val streak = cursor.getInt(cursor.getColumnIndexOrThrow("streak"))
            cursor.close()
            joke to streak
        } else {
            cursor.close()
            null
        }
    }

    fun getStreak(): Int {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT MAX(streak) FROM jokes", null)
        return if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
    }

    fun saveReactionState(joke: String, lol: Boolean, meh: Boolean) {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val lolState = if (lol) 1 else 0
        val mehState = if (meh) 1 else 0
        db.execSQL("UPDATE jokes SET reaction_lol = $lolState, reaction_meh = $mehState WHERE joke = ?", arrayOf(joke))
    }

    fun getReactionState(joke: String): Pair<Boolean, Boolean> {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT reaction_lol, reaction_meh FROM jokes WHERE joke = ?",
            arrayOf(joke)
        )
        val states = if (cursor.moveToFirst()) {
            Pair(cursor.getInt(0) == 1, cursor.getInt(1) == 1)
        } else {
            Pair(false, false)
        }
        cursor.close()
        return states
    }

}
