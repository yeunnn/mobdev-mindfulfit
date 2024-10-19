package com.example.s20.mobdev_project_mindfulfit.ui.motivation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.s20.mobdev_project_mindfulfit.databinding.FragmentDailyMotivationalContentBinding

class DailyMotivationalContentFragment : Fragment() {

    private var _binding: FragmentDailyMotivationalContentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dailyMotivationViewModel =
            ViewModelProvider(this).get(DailyMotivationalContentViewModel::class.java)

        _binding = FragmentDailyMotivationalContentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDailyMotivationalContent
        dailyMotivationViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}