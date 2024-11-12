package com.example.wifianalyser.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.wifianalyser.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UnRegisterReceiverViewModel @Inject constructor() : ViewModel() {

    val _receiverWatcher = MutableLiveData<Boolean>()
    val receiverWatcher: LiveData<Boolean> = _receiverWatcher

    val state = _receiverWatcher.map {
        if (!it) R.drawable.ic_pause
        else  R.drawable.ic_resume
    }

    fun unRegister(isRegister: Boolean) {
        _receiverWatcher.postValue(isRegister)
    }

}