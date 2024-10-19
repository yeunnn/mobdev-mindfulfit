package com.example.s20.mobdev_project_mindfulfit.ui.water

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WaterTrackerViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the Water Tracker Fragment"
    }
    val text: LiveData<String> = _text
}
