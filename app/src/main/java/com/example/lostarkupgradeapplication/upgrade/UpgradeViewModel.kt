package com.example.lostarkupgradeapplication.upgrade

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lostarkupgradeapplication.room.Equipment

class UpgradeViewModel: ViewModel() {
    val powerSeek: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val maxSeek: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val equipment: MutableLiveData<Equipment> by lazy {
        MutableLiveData<Equipment>()
    }

    init {
        powerSeek.value = 0
        maxSeek.value = 0
        equipment.value = Equipment(0, "", "", 0, 0, 0.0, 0, 1, 0)
    }
}