package com.example.lostarkupgradeapplication

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.lifecycle.ViewModel
import com.example.lostarkupgradeapplication.material.MaterialActivity

class MainViewModel: ViewModel() {
    fun openMaterial() {
        val intent = Intent(App.context(), MaterialActivity::class.java)
        App.context().startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
    }
}