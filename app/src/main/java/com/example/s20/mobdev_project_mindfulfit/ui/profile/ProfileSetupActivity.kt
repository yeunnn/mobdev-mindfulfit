package com.example.s20.mobdev_project_mindfulfit.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.s20.mobdev_project_mindfulfit.MainActivity
import com.example.s20.mobdev_project_mindfulfit.databinding.FragmentProfileSetupBinding

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: FragmentProfileSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding with the correct class
        binding = FragmentProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Done button click listener
        binding.btnDone.setOnClickListener {
            // Save setup completion status
            //val sharedPref = getSharedPreferences("ProfileSetup", Context.MODE_PRIVATE)
            //with(sharedPref.edit()) {
            //    putBoolean("isSetupComplete", true)
            //    apply()
            //}

            // Navigate to Home screen (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
