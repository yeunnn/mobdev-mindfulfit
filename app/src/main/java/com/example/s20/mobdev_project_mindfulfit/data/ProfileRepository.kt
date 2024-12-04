package com.example.s20.mobdev_project_mindfulfit.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class ProfileRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun saveProfile(username: String, waterGoal: Int, stepGoal: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USERNAME, username)
            put(DatabaseHelper.COLUMN_WATER_GOAL, waterGoal)
            put(DatabaseHelper.COLUMN_STEP_GOAL, stepGoal)
        }
        db.insert(DatabaseHelper.TABLE_PROFILE, null, values)
        db.close()
    }

    fun isProfileSetup(): Boolean {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_PROFILE}", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count > 0
    }
}
