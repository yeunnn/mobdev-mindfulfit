package com.example.s20.mobdev_project_mindfulfit.ui.humor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.s20.mobdev_project_mindfulfit.data.DatabaseHelper
import com.example.s20.mobdev_project_mindfulfit.data.HumorRepository
import com.example.s20.mobdev_project_mindfulfit.databinding.FragmentHumorForWellnessBinding
import com.example.s20.mobdev_project_mindfulfit.network.APIClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HumorForWellnessFragment : Fragment() {

    private var _binding: FragmentHumorForWellnessBinding? = null
    private val binding get() = _binding!!
    private lateinit var humorRepository: HumorRepository
    private lateinit var apiClient: APIClient

    private val humorTips = listOf(
        "Share a joke with someone today!",
        "Watch a comedy movie tonight!",
        "Start your day with a smile.",
        "Tell someone your favorite joke.",
        "Laugh—it’s good for the soul!"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHumorForWellnessBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val dbHelper = DatabaseHelper(requireContext())
        humorRepository = HumorRepository(dbHelper)
        apiClient = APIClient()

        setupUI()

        return root
    }

    private fun setupUI() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val existingJoke = humorRepository.getJoke(currentDate)
        if (existingJoke != null) {
            displayJoke(existingJoke.first, existingJoke.second)
        } else {
            toggleVisibility(isJokeDisplayed = false)
        }

        // Fetch Humor Button
        binding.btnFetchHumor.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                fetchAndSaveJoke(currentDate)
            }
        }
    }

    private suspend fun fetchAndSaveJoke(currentDate: String) {
        val fetchedJoke = withContext(Dispatchers.IO) { apiClient.fetchJoke() }

        if (fetchedJoke != null) {
            humorRepository.saveJoke(fetchedJoke, currentDate)
            val streak = humorRepository.getStreak()
            displayJoke(fetchedJoke, streak)
        } else {
            Toast.makeText(requireContext(), "Failed to fetch joke. Try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayJoke(joke: String, streak: Int) {
        binding.textJoke.text = joke
        binding.textLaughStreak.text = "You’ve read jokes for $streak days in a row!"
        binding.textHumorTip.text = "Humor Tip: ${humorTips.random()}"
        toggleVisibility(isJokeDisplayed = true)
    }

    private fun toggleVisibility(isJokeDisplayed: Boolean) {
        // Show or hide views based on whether the daily humor has been displayed
        binding.btnFetchHumor.visibility = if (isJokeDisplayed) View.GONE else View.VISIBLE
        binding.jokeDisplayContainer.visibility = if (isJokeDisplayed) View.VISIBLE else View.GONE
        binding.reactionLayout.visibility = if (isJokeDisplayed) View.VISIBLE else View.GONE
        binding.textHumorTip.visibility = if (isJokeDisplayed) View.VISIBLE else View.GONE
        binding.textLaughStreak.visibility = if (isJokeDisplayed) View.VISIBLE else View.GONE
        binding.btnShareJoke.visibility = if (isJokeDisplayed) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
