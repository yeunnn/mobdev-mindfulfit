package com.example.s20.mobdev_project_mindfulfit.ui.motivation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DailyMotivationalContentViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the Daily Motivational Content Fragment"
    }
    val text: LiveData<String> = _text
}
