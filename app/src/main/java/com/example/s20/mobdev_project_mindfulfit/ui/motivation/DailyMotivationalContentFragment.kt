package com.example.s20.mobdev_project_mindfulfit.ui.motivation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.s20.mobdev_project_mindfulfit.data.DatabaseHelper
import com.example.s20.mobdev_project_mindfulfit.data.QuotesRepository
import com.example.s20.mobdev_project_mindfulfit.databinding.FragmentDailyMotivationalContentBinding
import com.example.s20.mobdev_project_mindfulfit.network.APIClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DailyMotivationalContentFragment : Fragment() {

    private var _binding: FragmentDailyMotivationalContentBinding? = null
    private val binding get() = _binding!!
    private lateinit var quotesRepository: QuotesRepository
    private lateinit var apiClient: APIClient

    // List of mindfulness tips
    private val mindfulnessTips = listOf(
        "Mindfulness Tip: Take a deep breath and focus on the present.",
        "Mindfulness Tip: Go for a walk and enjoy the fresh air.",
        "Mindfulness Tip: Write down three things you're grateful for.",
        "Mindfulness Tip: Spend five minutes meditating quietly.",
        "Mindfulness Tip: Stretch your body and release tension.",
        "Mindfulness Tip: Take a moment to enjoy your favorite hobby.",
        "Mindfulness Tip: Disconnect from your devices for a while.",
        "Mindfulness Tip: Drink a glass of water and hydrate yourself.",
        "Mindfulness Tip: Practice a few minutes of mindful breathing.",
        "Mindfulness Tip: Compliment someone today, including yourself!"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyMotivationalContentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize the repository and API client
        val dbHelper = DatabaseHelper(requireContext())
        quotesRepository = QuotesRepository(dbHelper)
        apiClient = APIClient()

        setupUI()

        return root
    }

    private fun setupUI() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Check if a quote is already stored for today
        val existingQuote = quotesRepository.getQuote(currentDate)
        if (existingQuote != null) {
            displayQuote(existingQuote.first, existingQuote.second)
        } else {
            toggleVisibility(isQuoteDisplayed = false)
        }

        // Fetch quote button click listener
        binding.btnFetchQuote.setOnClickListener {
            lifecycleScope.launch {
                fetchAndSaveQuote(currentDate)
            }
        }
    }

    private suspend fun fetchAndSaveQuote(currentDate: String) {
        // Fetch quote from API
        val fetchedQuote = apiClient.fetchDailyQuote()

        if (fetchedQuote != null) {
            // Save the fetched quote and update the UI
            quotesRepository.saveQuote(fetchedQuote, currentDate)
            displayQuote(fetchedQuote, quotesRepository.getStreak())
        } else {
            Toast.makeText(requireContext(), "Failed to fetch quote. Try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayQuote(quote: String, streak: Int) {
        binding.textQuote.text = quote
        binding.textStreak.text = "You've read motivational quotes for $streak days straight!"
        binding.textMindfulnessTip.text = mindfulnessTips.random()
        toggleVisibility(isQuoteDisplayed = true)
    }

    private fun toggleVisibility(isQuoteDisplayed: Boolean) {
        // Show/hide views based on whether a quote is displayed
        binding.btnFetchQuote.visibility = if (isQuoteDisplayed) View.GONE else View.VISIBLE
        binding.textQuote.visibility = if (isQuoteDisplayed) View.VISIBLE else View.GONE
        binding.textMindfulnessTip.visibility = if (isQuoteDisplayed) View.VISIBLE else View.GONE
        binding.textStreak.visibility = if (isQuoteDisplayed) View.VISIBLE else View.GONE
        binding.quoteContainer.visibility = if (isQuoteDisplayed) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
