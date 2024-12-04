package com.example.s20.mobdev_project_mindfulfit.ui.water

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.s20.mobdev_project_mindfulfit.data.DatabaseHelper
import com.example.s20.mobdev_project_mindfulfit.databinding.DialogChangeWaterGoalBinding
import com.example.s20.mobdev_project_mindfulfit.databinding.DialogLogWaterIntakeBinding
import com.example.s20.mobdev_project_mindfulfit.databinding.FragmentWaterTrackerBinding
import java.text.SimpleDateFormat
import java.util.*

class WaterTrackerFragment : Fragment() {

    private var _binding: FragmentWaterTrackerBinding? = null
    private val binding get() = _binding!!
    private var waterGoal = 3000 // Default daily water goal (mL)
    private var totalWaterIntake = 0 // Today's total water intake (mL)
    private var lastWaterIntakeTime: Long? = null // Timestamp of the last water intake

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaterTrackerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Load data from database
        loadWaterData()

        // Log water intake button
        binding.waterIntakeLogButton.setOnClickListener {
            showLogWaterIntakeDialog()
        }

        // Change water intake goal button
        binding.btnChangeWaterGoal.setOnClickListener {
            showChangeWaterGoalDialog()
        }

        return root
    }

    private fun loadWaterData() {
        val dbHelper = DatabaseHelper(requireContext())
        val database = dbHelper.readableDatabase
        val todayDate = getCurrentDate()

        // Load water goal
        val goalCursor = database.rawQuery("SELECT water_goal FROM profile LIMIT 1", null)
        if (goalCursor.moveToFirst()) {
            waterGoal = goalCursor.getInt(0)
        }
        goalCursor.close()

        // Load today's water intake and last intake time
        val intakeCursor = database.rawQuery(
            "SELECT total_intake, last_intake_time FROM water_logs WHERE date = ?",
            arrayOf(todayDate)
        )
        if (intakeCursor.moveToFirst()) {
            totalWaterIntake = intakeCursor.getInt(0)
            lastWaterIntakeTime = intakeCursor.getLong(1)
        }
        intakeCursor.close()
        database.close()

        updateUI()
    }

    private fun updateUI() {
        // Update water intake and goal
        binding.waterIntakeCount.text = "$totalWaterIntake mL"
        binding.waterGoal.text = "Daily Goal: $waterGoal mL"
        binding.circularProgress.max = waterGoal
        binding.circularProgress.progress = totalWaterIntake

        // Update last drank time
        lastWaterIntakeTime?.let {
            val elapsedTime = (System.currentTimeMillis() - it) / (1000 * 60) // Minutes elapsed
            binding.timeOfWaterIntake.text = "LAST DRANK: $elapsedTime mins ago"
        } ?: run {
            binding.timeOfWaterIntake.text = "LAST DRANK: -- mins ago"
        }
    }

    private fun showLogWaterIntakeDialog() {
        val dialogBinding = DialogLogWaterIntakeBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Log Water Intake")
            .setView(dialogBinding.root)
            .setPositiveButton("Log") { _, _ ->
                val intakeAmount = dialogBinding.etWaterIntake.text.toString().toIntOrNull()
                if (intakeAmount != null && intakeAmount > 0) {
                    totalWaterIntake += intakeAmount
                    lastWaterIntakeTime = System.currentTimeMillis()
                    saveDailyProgress()
                    updateUI()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showChangeWaterGoalDialog() {
        val dialogBinding = DialogChangeWaterGoalBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Change Water Goal")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val newGoal = dialogBinding.etWaterGoal.text.toString().toIntOrNull()
                if (newGoal != null && newGoal > 0) {
                    waterGoal = newGoal
                    saveWaterGoal(newGoal)
                    updateUI()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun saveDailyProgress() {
        val dbHelper = DatabaseHelper(requireContext())
        val database = dbHelper.writableDatabase
        val todayDate = getCurrentDate()

        database.execSQL(
            "INSERT OR REPLACE INTO water_logs (date, total_intake, last_intake_time) VALUES (?, ?, ?)",
            arrayOf(todayDate, totalWaterIntake, lastWaterIntakeTime)
        )
        database.close()
    }

    private fun saveWaterGoal(newGoal: Int) {
        val dbHelper = DatabaseHelper(requireContext())
        val database = dbHelper.writableDatabase
        database.execSQL("UPDATE profile SET water_goal = ?", arrayOf(newGoal))
        database.close()
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
