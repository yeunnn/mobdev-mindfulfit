package com.example.s20.mobdev_project_mindfulfit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.s20.mobdev_project_mindfulfit.data.ProfileRepository
import com.example.s20.mobdev_project_mindfulfit.databinding.ActivityMainBinding
import com.example.s20.mobdev_project_mindfulfit.ui.profile.ProfileSetupActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var profileRepository: ProfileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profileRepository = ProfileRepository(this)

        // Redirect to Profile Setup if needed
        if (!profileRepository.isProfileSetup()) {
            val intent = Intent(this, ProfileSetupActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_steps_tracker,
                R.id.navigation_water_tracker,
                R.id.navigation_daily_motivational_content,
                R.id.navigation_humor_for_wellness
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}
