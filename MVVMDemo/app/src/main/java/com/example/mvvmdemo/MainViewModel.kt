package com.example.mvvmdemo


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    val counter = MutableLiveData<Int>()

    fun onIncrementClicked() {
        counter.value = (counter.value ?: 0) + 1
    }
}