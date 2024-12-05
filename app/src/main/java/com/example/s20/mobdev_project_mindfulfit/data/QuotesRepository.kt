package com.example.s20.mobdev_project_mindfulfit.data

import android.database.sqlite.SQLiteDatabase

class QuotesRepository(private val dbHelper: DatabaseHelper) {

    fun getQuote(date: String): Pair<String, Int>? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val query = "SELECT quote, streak FROM quotes WHERE date = ?"
        db.rawQuery(query, arrayOf(date)).use { cursor ->
            if (cursor.moveToFirst()) {
                val quote = cursor.getString(0)
                val streak = cursor.getInt(1)
                return Pair(quote, streak)
            }
        }
        return null
    }

    fun saveQuote(quote: String, date: String) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val currentStreak = getStreak()
        val query = """
            INSERT OR REPLACE INTO quotes (quote, date, streak) 
            VALUES (?, ?, ?)
        """
        db.execSQL(query, arrayOf(quote, date, currentStreak + 1))
    }

    fun getStreak(): Int {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val query = "SELECT MAX(streak) FROM quotes"
        db.rawQuery(query, null).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
        }
        return 0
    }

    fun saveFavoriteState(quote: String, isFavorite: Boolean) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val state = if (isFavorite) 1 else 0
        db.execSQL("UPDATE quotes SET is_favorite = $state WHERE quote = ?", arrayOf(quote))
    }

    fun getFavoriteState(quote: String): Boolean {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val cursor = db.rawQuery("SELECT is_favorite FROM quotes WHERE quote = ?", arrayOf(quote))
        val isFavorite = if (cursor.moveToFirst()) cursor.getInt(0) == 1 else false
        cursor.close()
        return isFavorite
    }

}
