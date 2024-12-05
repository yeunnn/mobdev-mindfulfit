package com.example.s20.mobdev_project_mindfulfit.ui.motivation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.s20.mobdev_project_mindfulfit.R
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

    private var currentQuote: String? = null // Track the current displayed quote
    private var isFavorite: Boolean = false // Track the favorite state

    // List of mindfulness tips
    private val mindfulnessTips = listOf(
        "Mindfulness Tip: Take a deep breath and focus on the present.",
        "Mindfulness Tip: Go for a walk and enjoy the fresh air.",
        "Mindfulness Tip: Write down three things you're grateful for.",
        "Mindfulness Tip: Spend five minutes meditating quietly.",
        "Mindfulness Tip: Stretch your body and release tension."
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
            isFavorite = quotesRepository.getFavoriteState(existingQuote.first)
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

        // Favorite button click listener
        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }

        // Share button click listener
        binding.btnShareQuote.setOnClickListener {
            shareQuote()
        }
    }

    private suspend fun fetchAndSaveQuote(currentDate: String) {
        val fetchedQuote = apiClient.fetchDailyQuote()

        if (fetchedQuote != null) {
            quotesRepository.saveQuote(fetchedQuote, currentDate)
            currentQuote = fetchedQuote
            isFavorite = false
            quotesRepository.saveFavoriteState(fetchedQuote, false)
            updateFavoriteIcon()
            displayQuote(fetchedQuote, quotesRepository.getStreak())
        } else {
            Toast.makeText(requireContext(), "Failed to fetch quote. Try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayQuote(quote: String, streak: Int) {
        currentQuote = quote
        binding.textQuote.text = quote
        binding.textStreak.text = "You've read motivational quotes for $streak days straight!"
        binding.textMindfulnessTip.text = mindfulnessTips.random()
        updateFavoriteIcon()
        toggleVisibility(isQuoteDisplayed = true)
    }

    private fun toggleVisibility(isQuoteDisplayed: Boolean) {
        binding.btnFetchQuote.visibility = if (isQuoteDisplayed) View.GONE else View.VISIBLE
        binding.textQuote.visibility = if (isQuoteDisplayed) View.VISIBLE else View.GONE
        binding.textMindfulnessTip.visibility = if (isQuoteDisplayed) View.VISIBLE else View.GONE
        binding.textStreak.visibility = if (isQuoteDisplayed) View.VISIBLE else View.GONE
        binding.quoteContainer.visibility = if (isQuoteDisplayed) View.VISIBLE else View.GONE
    }

    private fun toggleFavorite() {
        isFavorite = !isFavorite
        currentQuote?.let { quotesRepository.saveFavoriteState(it, isFavorite) }
        updateFavoriteIcon()

        val message = if (isFavorite) "You liked this quote!" else "You unliked this quote!"
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun updateFavoriteIcon() {
        val icon = if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_outline
        binding.btnFavorite.setImageResource(icon)
    }

    private fun shareQuote() {
        currentQuote?.let { quote ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Here's an inspiring quote: \"$quote\"")
            }
            startActivity(Intent.createChooser(intent, "Share via"))
        } ?: Toast.makeText(requireContext(), "No quote to share!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
