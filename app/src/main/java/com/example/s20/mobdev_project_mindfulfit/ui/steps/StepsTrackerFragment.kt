package com.example.s20.mobdev_project_mindfulfit.ui.steps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.s20.mobdev_project_mindfulfit.databinding.FragmentStepsTrackerBinding

class StepsTrackerFragment : Fragment() {

    private var _binding: FragmentStepsTrackerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val stepsTrackerViewModel =
            ViewModelProvider(this).get(StepsTrackerViewModel::class.java)

        _binding = FragmentStepsTrackerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textStepsTracker
        stepsTrackerViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
