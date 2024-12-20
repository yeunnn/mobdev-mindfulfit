package com.example.s20.mobdev_project_mindfulfit.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "MindfulFit.db"
        const val DATABASE_VERSION = 1
        const val TABLE_PROFILE = "profile"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_WATER_GOAL = "water_goal"
        const val COLUMN_STEP_GOAL = "step_goal"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_PROFILE_TABLE = """
            CREATE TABLE $TABLE_PROFILE (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT,
                $COLUMN_WATER_GOAL INTEGER,
                $COLUMN_STEP_GOAL INTEGER
            )
        """
        db.execSQL(CREATE_PROFILE_TABLE)

        // New table for daily motivational quotes
        val CREATE_QUOTES_TABLE = """
        CREATE TABLE quotes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            quote TEXT,
            date TEXT UNIQUE,
            streak INTEGER,
            is_favorite INTEGER DEFAULT 0
        )
        """
        db.execSQL(CREATE_QUOTES_TABLE)

        // New table for daily jokes
        val CREATE_JOKES_TABLE = """
            CREATE TABLE jokes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                joke TEXT,
                date TEXT UNIQUE,
                streak INTEGER,
                reaction_lol INTEGER DEFAULT 0,
                reaction_meh INTEGER DEFAULT 0
            )
        """
        db.execSQL(CREATE_JOKES_TABLE)

        // New table for daily steps tracker
        val CREATE_DAILY_STEPS_TABLE = """
        CREATE TABLE daily_steps (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            date TEXT UNIQUE,
            steps INTEGER,
            distance REAL,
            calories REAL
        )
    """
        db.execSQL(CREATE_DAILY_STEPS_TABLE)

        // New table for daily steps tracker
        val CREATE_DAILY_WATER_TABLE = """
        CREATE TABLE water_logs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            date TEXT UNIQUE,
            total_intake INTEGER DEFAULT 0,
            last_intake_time INTEGER
        )
        """
        db.execSQL(CREATE_DAILY_WATER_TABLE)

        // Insert dummy data
        //insertDummyData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        onCreate(db)
    }

    // STEP TRACKER
    fun getStepGoal(): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT step_goal FROM step_tracker WHERE id = 1", null)
        return if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else null
    }

    fun updateStepGoal(newGoal: Int) {
        val db = writableDatabase
        db.execSQL("UPDATE step_tracker SET step_goal = $newGoal WHERE id = 1")
    }

    fun saveSteps(date: String, steps: Int) {
        val db = writableDatabase
        db.execSQL("INSERT OR REPLACE INTO daily_steps (date, steps) VALUES ('$date', $steps)")
    }
    // END - STEP TRACKER

    private fun insertDummyData(db: SQLiteDatabase) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        for (i in 0 until 35) {
            val date = sdf.format(calendar.time)
            val steps = (3000..10000).random()
            val distance = steps * 0.0008 // Approx 0.8 meters per step
            val calories = steps * 0.04 // Approx 0.04 kcal per step
            val waterIntake = (500..3000).random()

            // Insert into daily_steps
            val insertStepsQuery = """
                INSERT OR REPLACE INTO daily_steps (date, steps, distance, calories)
                VALUES ('$date', $steps, $distance, $calories)
            """
            db.execSQL(insertStepsQuery)

            // Insert into water_logs
            val insertWaterQuery = """
                INSERT OR REPLACE INTO water_logs (date, total_intake)
                VALUES ('$date', $waterIntake)
            """
            db.execSQL(insertWaterQuery)

            calendar.add(Calendar.DATE, -1) // Move to the previous day
        }

        Log.d("DatabaseHelper", "Dummy data inserted successfully")
    }
}
