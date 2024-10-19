package com.example.s20.mobdev_project_mindfulfit.ui.humor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.s20.mobdev_project_mindfulfit.R
import com.example.s20.mobdev_project_mindfulfit.databinding.FragmentHumorForWellnessBinding

class HumorForWellnessFragment : Fragment() {

    private var _binding: FragmentHumorForWellnessBinding? = null
    private val binding get() = _binding!!
    private var isLolClicked = false
    private var isMehClicked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        val humorViewModel =
            ViewModelProvider(this).get(HumorForWellnessViewModel::class.java)

        _binding = FragmentHumorForWellnessBinding.inflate(inflater, container, false)

        // Set up LOL reaction button click listener
        binding.btnReactionLol.setOnClickListener {
            isLolClicked = !isLolClicked
            if (isLolClicked) {
                binding.btnReactionLol.setImageResource(R.drawable.ic_lol)
            } else {
                binding.btnReactionLol.setImageResource(R.drawable.ic_lol_outline)
            }
        }

        // Set up Meh reaction button click listener
        binding.btnReactionMeh.setOnClickListener {
            isMehClicked = !isMehClicked
            if (isMehClicked) {
                binding.btnReactionMeh.setImageResource(R.drawable.ic_meh)
            } else {
                binding.btnReactionMeh.setImageResource(R.drawable.ic_meh_outline)
            }
        }

        val root: View = binding.root

        //val textView: TextView = binding.textHumorForWellness
        //.text.observe(viewLifecycleOwner) {
        //    textView.text = it
        //}
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
