package com.example.lostarkupgradeapplication.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingViewModel: ViewModel() {
    val version: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}