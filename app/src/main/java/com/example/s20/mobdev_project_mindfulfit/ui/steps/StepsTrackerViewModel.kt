package com.example.s20.mobdev_project_mindfulfit.ui.steps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StepsTrackerViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the Steps Tracker Fragment"
    }
    val text: LiveData<String> = _text
}
