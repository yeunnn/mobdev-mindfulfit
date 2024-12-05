package com.example.s20.mobdev_project_mindfulfit.ui.home

import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.s20.mobdev_project_mindfulfit.data.DatabaseHelper
import com.example.s20.mobdev_project_mindfulfit.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var waterGoal = 0
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

        // Initialize UI and load data
        initializeUI()
        loadAndDisplayData()

        return root
    }

    override fun onResume() {
        super.onResume()
        initializeUI()
        loadAndDisplayData()
    }

    private fun initializeUI() {
        binding.textName.text = "Loading..."
        binding.waterIntakeValue.text = "-- mL"
        binding.waterCircularProgress.max = 1
        binding.waterCircularProgress.progress = 0
        binding.waterIntakeGoal.text = "Goal: -- mL"
        binding.stepsValue.text = "-- steps"
        binding.stepsCircularProgress.max = 1
        binding.stepsCircularProgress.progress = 0
        binding.stepsGoal.text = "Goal: -- steps"
        binding.remindersContent.text = "Fetching your reminders..."
    }

    private fun loadAndDisplayData() {
        loadGoals()
        loadProgress()
        updateUI()
        loadWeeklyReport()
        loadMonthlyReport()
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

        val stepsCursor: Cursor = db.rawQuery(
            "SELECT steps FROM daily_steps WHERE date = ?",
            arrayOf(currentDate)
        )
        if (stepsCursor.moveToFirst()) {
            stepsTaken = stepsCursor.getInt(0)
        }
        stepsCursor.close()

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
        binding.textName.text = username
        binding.waterIntakeValue.text = "$waterIntake mL"
        binding.waterCircularProgress.max = waterGoal
        binding.waterCircularProgress.progress = waterIntake
        binding.waterIntakeGoal.text = "Goal: $waterGoal mL"

        binding.stepsValue.text = "$stepsTaken steps"
        binding.stepsCircularProgress.max = stepGoal
        binding.stepsCircularProgress.progress = stepsTaken
        binding.stepsGoal.text = "Goal: $stepGoal steps"

        val waterPercentage = if (waterGoal > 0) (waterIntake.toFloat() / waterGoal * 100).toInt() else 0
        val stepPercentage = if (stepGoal > 0) (stepsTaken.toFloat() / stepGoal * 100).toInt() else 0

        binding.remindersContent.text = when {
            waterPercentage < 100 && stepPercentage < 100 ->
                "You have reached $waterPercentage% of your water intake goal and $stepPercentage% of your step goal. Keep going! \nBe sure to log both your activities in the Steps and Water Tracker!"
            waterPercentage < 100 ->
                "You have reached $waterPercentage% of your water intake goal. Keep going! \nBe sure to log both your activities in the Steps and Water Tracker!"
            stepPercentage < 100 ->
                "You have reached $stepPercentage% of your step goal. Keep going! \nBe sure to log both your activities in the Steps and Water Tracker!"
            else ->
                "Congratulations! You've completed your goals for today!"
        }
    }

    private fun loadWeeklyReport() {
        val db = databaseHelper.readableDatabase
        val query = """
            SELECT 
                daily_steps.date AS step_date, 
                water_logs.total_intake, 
                daily_steps.steps 
            FROM 
                daily_steps 
            INNER JOIN 
                water_logs 
            ON 
                daily_steps.date = water_logs.date 
            ORDER BY 
                daily_steps.date DESC 
            LIMIT 7;
        """
        val cursor = db.rawQuery(query, null)
        val waterEntries = mutableListOf<BarEntry>()
        val stepsEntries = mutableListOf<BarEntry>()
        var dayIndex = 0

        if (cursor.count >= 7) {
            while (cursor.moveToNext()) {
                waterEntries.add(BarEntry(dayIndex.toFloat(), cursor.getFloat(1)))
                stepsEntries.add(BarEntry(dayIndex.toFloat(), cursor.getFloat(2)))
                dayIndex++
            }
            binding.weeklyReportContent.visibility = View.GONE
            binding.weeklyWaterChart.visibility = View.VISIBLE
            binding.weeklyStepsChart.visibility = View.VISIBLE
            setupBarChart(binding.weeklyWaterChart, "Water Intake (mL)", waterEntries, Color.BLUE)
            setupBarChart(binding.weeklyStepsChart, "Steps Taken", stepsEntries, Color.GREEN)
        } else {
            binding.weeklyReportContent.visibility = View.VISIBLE
            binding.weeklyWaterChart.visibility = View.GONE
            binding.weeklyStepsChart.visibility = View.GONE
            binding.weeklyReportContent.text = "Not enough data for Weekly Report. Keep progressing!"
        }
        cursor.close()
        db.close()
    }

    private fun loadMonthlyReport() {
        val db = databaseHelper.readableDatabase
        val query = """
            SELECT 
                daily_steps.date AS step_date, 
                water_logs.total_intake, 
                daily_steps.steps 
            FROM 
                daily_steps 
            INNER JOIN 
                water_logs 
            ON 
                daily_steps.date = water_logs.date 
            ORDER BY 
                daily_steps.date DESC 
            LIMIT 30;
        """
        val cursor = db.rawQuery(query, null)
        val waterEntries = mutableListOf<BarEntry>()
        val stepsEntries = mutableListOf<BarEntry>()
        var dayIndex = 0

        if (cursor.count >= 30) {
            while (cursor.moveToNext()) {
                waterEntries.add(BarEntry(dayIndex.toFloat(), cursor.getFloat(1)))
                stepsEntries.add(BarEntry(dayIndex.toFloat(), cursor.getFloat(2)))
                dayIndex++
            }
            binding.monthlyReportContent.visibility = View.GONE
            binding.monthlyWaterChart.visibility = View.VISIBLE
            binding.monthlyStepsChart.visibility = View.VISIBLE
            setupBarChart(binding.monthlyWaterChart, "Water Intake (mL)", waterEntries, Color.BLUE)
            setupBarChart(binding.monthlyStepsChart, "Steps Taken", stepsEntries, Color.GREEN)
        } else {
            binding.monthlyReportContent.visibility = View.VISIBLE
            binding.monthlyWaterChart.visibility = View.GONE
            binding.monthlyStepsChart.visibility = View.GONE
            binding.monthlyReportContent.text = "Not enough data for Monthly Report. Keep progressing!"
        }
        cursor.close()
        db.close()
    }

    private fun setupBarChart(chart: BarChart, label: String, entries: List<BarEntry>, color: Int) {
        val dataSet = BarDataSet(entries, label)
        dataSet.color = color
        val barData = BarData(dataSet)
        barData.barWidth = 0.8f
        chart.data = barData
        chart.setFitBars(true)
        chart.invalidate()

        val description = Description()
        description.text = ""
        chart.description = description
        chart.axisLeft.granularity = 1f
        chart.axisRight.isEnabled = false
        chart.xAxis.isEnabled = false
        chart.legend.isEnabled = true
        chart.animateY(1000)
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
