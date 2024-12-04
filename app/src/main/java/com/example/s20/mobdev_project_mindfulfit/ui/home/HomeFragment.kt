package com.example.s20.mobdev_project_mindfulfit.ui.home

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.s20.mobdev_project_mindfulfit.data.DatabaseHelper
import com.example.s20.mobdev_project_mindfulfit.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var waterGoal = 0 // Default to 0 to avoid showing incorrect placeholder values
    private var stepGoal = 0
    private var waterIntake = 0
    private var stepsTaken = 0
    private var username = ""
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        databaseHelper = DatabaseHelper(requireContext())
        val root: View = binding.root

        // Initialize UI with neutral/default states
        initializeUI()

        // Load data from the database and update the UI
        loadAndDisplayData()

        return root
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when the fragment becomes visible
        initializeUI() // Reset UI to neutral state
        loadAndDisplayData() // Reload data and update the UI
    }

    private fun initializeUI() {
        // Set UI elements to empty/neutral state
        binding.textName.text = "Loading..."
        binding.waterIntakeValue.text = "-- mL"
        binding.waterCircularProgress.max = 1 // Avoid division by zero; adjust later
        binding.waterCircularProgress.progress = 0
        binding.waterIntakeGoal.text = "Goal: -- mL"
        binding.stepsValue.text = "-- steps"
        binding.stepsCircularProgress.max = 1
        binding.stepsCircularProgress.progress = 0
        binding.stepsGoal.text = "Goal: -- steps"
        binding.remindersContent.text = "Fetching your reminders..."
    }

    private fun loadAndDisplayData() {
        // Load goals and progress from the database
        loadGoals()
        loadProgress()

        // Update the UI with the loaded data
        updateUI()
    }

    private fun loadGoals() {
        val db = databaseHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT username, water_goal, step_goal FROM profile LIMIT 1", null)
        if (cursor.moveToFirst()) {
            username = cursor.getString(0)
            waterGoal = cursor.getInt(1)
            stepGoal = cursor.getInt(2)
        }
        cursor.close()
        db.close()
    }

    private fun loadProgress() {
        val db = databaseHelper.readableDatabase
        val currentDate = getCurrentDate()

        // Load daily steps
        val stepsCursor: Cursor = db.rawQuery(
            "SELECT steps FROM daily_steps WHERE date = ?",
            arrayOf(currentDate)
        )
        if (stepsCursor.moveToFirst()) {
            stepsTaken = stepsCursor.getInt(0)
        }
        stepsCursor.close()

        // Load daily water intake
        val waterCursor: Cursor = db.rawQuery(
            "SELECT total_intake FROM water_logs WHERE date = ?",
            arrayOf(currentDate)
        )
        if (waterCursor.moveToFirst()) {
            waterIntake = waterCursor.getInt(0)
        }
        waterCursor.close()

        db.close()
    }

    private fun updateUI() {
        // Set username
        binding.textName.text = username

        // Update water intake progress
        binding.waterIntakeValue.text = "$waterIntake mL"
        binding.waterCircularProgress.max = waterGoal
        binding.waterCircularProgress.progress = waterIntake
        binding.waterIntakeGoal.text = "Goal: $waterGoal mL"

        // Update step progress
        binding.stepsValue.text = "$stepsTaken steps"
        binding.stepsCircularProgress.max = stepGoal
        binding.stepsCircularProgress.progress = stepsTaken
        binding.stepsGoal.text = "Goal: $stepGoal steps"

        // Update reminders
        val waterPercentage = if (waterGoal > 0) (waterIntake.toFloat() / waterGoal * 100).toInt() else 0
        val stepPercentage = if (stepGoal > 0) (stepsTaken.toFloat() / stepGoal * 100).toInt() else 0

        binding.remindersContent.text = when {
            waterPercentage < 100 && stepPercentage < 100 ->
                "You have reached $waterPercentage% of your water intake goal and $stepPercentage% of your step goal. Keep going!"
            waterPercentage < 100 ->
                "You have reached $waterPercentage% of your water intake goal. Keep going!"
            stepPercentage < 100 ->
                "You have reached $stepPercentage% of your step goal. Keep going!"
            else ->
                "Congratulations! You've completed your goals for today!"
        }
    }

    private fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
