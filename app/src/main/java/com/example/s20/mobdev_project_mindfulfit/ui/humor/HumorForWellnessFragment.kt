package com.example.s20.mobdev_project_mindfulfit.ui.humor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.s20.mobdev_project_mindfulfit.R
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

    private var currentJoke: String? = null // To store the current joke
    private var isLolSelected: Boolean = false
    private var isMehSelected: Boolean = false

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

        // Check if a joke is already stored for today
        val existingJoke = humorRepository.getJoke(currentDate)
        if (existingJoke != null) {
            val reactionStates = humorRepository.getReactionState(existingJoke.first)
            isLolSelected = reactionStates.first
            isMehSelected = reactionStates.second
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

        // LOL Reaction Button
        binding.btnReactionLol.setOnClickListener {
            if (!isLolSelected) {
                isLolSelected = true
                isMehSelected = false
                saveReaction()
                updateReactionIcons()
                Toast.makeText(requireContext(), "Glad you enjoyed the joke!", Toast.LENGTH_SHORT).show()
            }
        }

        // Meh Reaction Button
        binding.btnReactionMeh.setOnClickListener {
            if (!isMehSelected) {
                isMehSelected = true
                isLolSelected = false
                saveReaction()
                updateReactionIcons()
                Toast.makeText(requireContext(), "We'll do better next time!", Toast.LENGTH_SHORT).show()
            }
        }

        // Share Joke Button
        binding.btnShareJoke.setOnClickListener {
            shareJoke()
        }
    }

    private suspend fun fetchAndSaveJoke(currentDate: String) {
        val fetchedJoke = withContext(Dispatchers.IO) { apiClient.fetchJoke() }

        if (fetchedJoke != null) {
            humorRepository.saveJoke(fetchedJoke, currentDate)
            val streak = humorRepository.getStreak()
            currentJoke = fetchedJoke
            resetReactions() // Reset reactions for the new joke
            displayJoke(fetchedJoke, streak)
        } else {
            Toast.makeText(requireContext(), "Failed to fetch joke. Try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayJoke(joke: String, streak: Int) {
        currentJoke = joke
        binding.textJoke.text = joke
        binding.textLaughStreak.text = "You’ve read jokes for $streak days in a row!"
        binding.textHumorTip.text = "Humor Tip: ${humorTips.random()}"
        updateReactionIcons()
        toggleVisibility(isJokeDisplayed = true)
    }

    private fun toggleVisibility(isJokeDisplayed: Boolean) {
        binding.btnFetchHumor.visibility = if (isJokeDisplayed) View.GONE else View.VISIBLE
        binding.jokeDisplayContainer.visibility = if (isJokeDisplayed) View.VISIBLE else View.GONE
        binding.reactionLayout.visibility = if (isJokeDisplayed) View.VISIBLE else View.GONE
        binding.textHumorTip.visibility = if (isJokeDisplayed) View.VISIBLE else View.GONE
        binding.textLaughStreak.visibility = if (isJokeDisplayed) View.VISIBLE else View.GONE
        binding.btnShareJoke.visibility = if (isJokeDisplayed) View.VISIBLE else View.GONE
    }

    private fun updateReactionIcons() {
        binding.btnReactionLol.setImageResource(
            if (isLolSelected) R.drawable.ic_lol else R.drawable.ic_lol_outline
        )
        binding.btnReactionMeh.setImageResource(
            if (isMehSelected) R.drawable.ic_meh else R.drawable.ic_meh_outline
        )
    }

    private fun saveReaction() {
        currentJoke?.let { joke ->
            humorRepository.saveReactionState(joke, isLolSelected, isMehSelected)
        }
    }

    private fun resetReactions() {
        isLolSelected = false
        isMehSelected = false
        updateReactionIcons()
    }

    private fun shareJoke() {
        currentJoke?.let { joke ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Here's a funny joke: \"$joke\"")
            }
            startActivity(Intent.createChooser(intent, "Share via"))
        } ?: Toast.makeText(requireContext(), "No joke to share!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
