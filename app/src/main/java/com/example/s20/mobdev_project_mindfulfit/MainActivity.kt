package com.example.s20.mobdev_project_mindfulfit

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.s20.mobdev_project_mindfulfit.databinding.ActivityMainBinding
import com.example.s20.mobdev_project_mindfulfit.ui.profile.ProfileSetupActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if profile setup is done
        //val sharedPref = getSharedPreferences("ProfileSetup", Context.MODE_PRIVATE)
        //val isSetupComplete = sharedPref.getBoolean("isSetupComplete", false)

        //if (!isSetupComplete) {
            // Navigate to Profile Setup if not done
        //    val intent = Intent(this, ProfileSetupActivity::class.java)
        //    startActivity(intent)
        //    finish() // Close MainActivity to prevent going back to it
        //return // Exit onCreate to prevent further initialization
        //}

        //temp
        val intent = Intent(this, ProfileSetupActivity::class.java)
        startActivity(intent)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of IDs because each
        // menu should be considered as top-level destinations.
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
