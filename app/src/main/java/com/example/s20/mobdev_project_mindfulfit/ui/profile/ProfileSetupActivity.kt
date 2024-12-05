package com.example.s20.mobdev_project_mindfulfit.ui.profile

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.s20.mobdev_project_mindfulfit.MainActivity
import com.example.s20.mobdev_project_mindfulfit.data.ProfileRepository
import com.example.s20.mobdev_project_mindfulfit.databinding.FragmentProfileSetupBinding
import java.io.File

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: FragmentProfileSetupBinding
    private lateinit var profileRepository: ProfileRepository
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profileRepository = ProfileRepository(this)

        // If profile is already set up, navigate to the main activity
        if (profileRepository.isProfileSetup()) {
            navigateToHome()
            return
        }

        binding = FragmentProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up image selection
        setupImageSelection()

        // Set click listener for avatar
        binding.imgUserAvatar.setOnClickListener { showImageSourceDialog() }

        // Done button logic
        binding.btnDone.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val waterGoal = binding.etWaterIntake.text.toString().trim().toIntOrNull() ?: 0
            val stepGoal = binding.etStepGoal.text.toString().trim().toIntOrNull() ?: 0

            if (username.isNotEmpty() && waterGoal > 0 && stepGoal > 0) {
                profileRepository.saveProfile(username, waterGoal, stepGoal)
                navigateToHome()
            } else {
                binding.etUsername.error = "Please fill in all fields"
            }
        }
    }

    private fun setupImageSelection() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                photoFile?.let { file ->
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    binding.imgUserAvatar.setImageBitmap(bitmap)
                }
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val selectedImageUri: Uri? = it.data?.data
                binding.imgUserAvatar.setImageURI(selectedImageUri)
            }
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Gallery")
        AlertDialog.Builder(this)
            .setTitle("Select Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
//                    1 -> openCamera()
                }
            }
            .show()
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

//    private fun openCamera() {
//        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        val tempFile = File.createTempFile("avatar_", ".jpg", externalCacheDir)
//        photoFile = tempFile
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile))
//        cameraLauncher.launch(cameraIntent)
//    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}