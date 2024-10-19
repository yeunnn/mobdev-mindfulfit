package com.example.s20.mobdev_project_mindfulfit.ui.humor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HumorForWellnessViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the Humor for Wellness Fragment"
    }
    val text: LiveData<String> = _text
}
