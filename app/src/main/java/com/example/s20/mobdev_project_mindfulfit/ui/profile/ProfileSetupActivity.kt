package com.example.s20.mobdev_project_mindfulfit.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.s20.mobdev_project_mindfulfit.MainActivity
import com.example.s20.mobdev_project_mindfulfit.data.ProfileRepository
import com.example.s20.mobdev_project_mindfulfit.databinding.FragmentProfileSetupBinding

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: FragmentProfileSetupBinding
    private lateinit var profileRepository: ProfileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profileRepository = ProfileRepository(this)

        // Check if profile setup is already complete
        if (profileRepository.isProfileSetup()) {
            navigateToHome()
            return
        }

        binding = FragmentProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDone.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val waterGoal = binding.etWaterIntake.text.toString().trim().toIntOrNull() ?: 0
            val stepGoal = binding.etStepGoal.text.toString().trim().toIntOrNull() ?: 0

            if (username.isNotEmpty() && waterGoal > 0 && stepGoal > 0) {
                // Save data to SQLite
                profileRepository.saveProfile(username, waterGoal, stepGoal)

                // Navigate to Home
                navigateToHome()
            } else {
                // Show error (optional)
                binding.etUsername.error = "Please fill in all fields"
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
