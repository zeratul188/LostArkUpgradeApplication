package com.example.lostarkupgradeapplication

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lostarkupgradeapplication.material.MaterialActivity

class MainViewModel: ViewModel() {
    val allLevel: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }

    init {
        allLevel.value = 0.0
    }

    fun openMaterial() {
        val intent = Intent(App.context(), MaterialActivity::class.java)
        App.context().startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
    }
}