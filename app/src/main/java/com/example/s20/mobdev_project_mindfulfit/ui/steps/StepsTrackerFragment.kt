package com.example.s20.mobdev_project_mindfulfit.ui.steps

import android.app.AlertDialog
import android.content.ContentValues
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.s20.mobdev_project_mindfulfit.data.DatabaseHelper
import com.example.s20.mobdev_project_mindfulfit.databinding.DialogChangeStepGoalBinding
import com.example.s20.mobdev_project_mindfulfit.databinding.FragmentStepsTrackerBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class StepsTrackerFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentStepsTrackerBinding? = null
    private val binding get() = _binding!!
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var stepCount = 0
    private var stepGoal = 6000
    private var distanceCovered = 0.0 // In kilometers
    private var caloriesBurned = 0.0
    private var lastAcceleration = 0f
    private var currentAcceleration = 0f
    private var stepThreshold = 12f
    private var lastStepTime: Long = 0
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var currentDate: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepsTrackerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize SensorManager
        sensorManager = requireContext().getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        dbHelper = DatabaseHelper(requireContext())

        // Get today's date
        currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Initialize step goal and load daily progress
        loadStepGoal()
        loadDailyProgress()

        // Change Step Goal Button
        binding.btnChangeStepGoal.setOnClickListener {
            showChangeStepGoalDialog()
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = currentAcceleration - lastAcceleration
            lastAcceleration = currentAcceleration

            // Detect steps based on a threshold
            if (delta > stepThreshold && (System.currentTimeMillis() - lastStepTime) > 300) {
                stepCount++
                lastStepTime = System.currentTimeMillis()
                updateProgress()
                saveDailyProgress()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed
    }

    private fun updateProgress() {
        // Calculate distance and calories burned
        distanceCovered = stepCount * 0.000762 // Assume an average stride length of 0.762 meters
        caloriesBurned = stepCount * 0.04 // Average calories burned per step

        // Update UI
        binding.stepsCount.text = "$stepCount steps"
        binding.stepsGoal.text = "Set Goal: $stepGoal steps"
        binding.textDistanceCovered.text = "DISTANCE COVERED: ${"%.2f".format(distanceCovered)} Km"
        binding.textCaloriesBurned.text = "CALORIES BURNED: ${"%.1f".format(caloriesBurned)} kcal"
        binding.circularProgress.max = stepGoal
        binding.circularProgress.progress = stepCount
        val currentTempDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(System.currentTimeMillis())
        binding.timeOfSteps.text = "DATE FOR TODAY: $currentTempDate"
    }

    private fun loadStepGoal() {
        val database = dbHelper.readableDatabase
        val cursor = database.rawQuery("SELECT step_goal FROM profile LIMIT 1", null)
        if (cursor.moveToFirst()) {
            stepGoal = cursor.getInt(0)
        }
        cursor.close()
        database.close()
    }

    private fun loadDailyProgress() {
        val database = dbHelper.readableDatabase
        val cursor = database.rawQuery("SELECT steps, distance, calories FROM daily_steps WHERE date = ?", arrayOf(currentDate))
        if (cursor.moveToFirst()) {
            stepCount = cursor.getInt(0)
            distanceCovered = cursor.getDouble(1)
            caloriesBurned = cursor.getDouble(2)
            updateProgress()
        } else {
            resetDailyProgress()
        }
        cursor.close()
        database.close()
    }

    private fun saveDailyProgress() {
        val database = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("date", currentDate)
            put("steps", stepCount)
            put("distance", distanceCovered)
            put("calories", caloriesBurned)
        }

        val rows = database.update("daily_steps", values, "date = ?", arrayOf(currentDate))
        if (rows == 0) {
            database.insert("daily_steps", null, values)
        }

        database.close()
    }

    private fun resetDailyProgress() {
        stepCount = 0
        distanceCovered = 0.0
        caloriesBurned = 0.0
        updateProgress()
    }

    private fun showChangeStepGoalDialog() {
        // Inflate the dialog layout
        val dialogBinding = DialogChangeStepGoalBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Change Step Goal")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val newGoal = dialogBinding.etStepGoal.text.toString().toIntOrNull()
                if (newGoal != null && newGoal > 0) {
                    stepGoal = newGoal
                    saveStepGoal(newGoal)
                    updateProgress()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun saveStepGoal(newGoal: Int) {
        val database = dbHelper.writableDatabase
        database.execSQL("UPDATE profile SET step_goal = $newGoal")
        database.close()
    }
}
