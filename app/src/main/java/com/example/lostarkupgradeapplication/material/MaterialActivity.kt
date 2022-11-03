
package com.example.lostarkupgradeapplication.material

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.lostarkupgradeapplication.R
import com.example.lostarkupgradeapplication.databinding.ActivityMaterialBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable

class MaterialActivity : AppCompatActivity() {
    private val viewModel: MaterialViewModel by viewModels()
    private lateinit var binding: ActivityMaterialBinding

    private var myCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_material)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_material)
        binding.materialViewModel = viewModel
    }

    override fun onDestroy() {
        myCompositeDisposable.clear()
        super.onDestroy()
    }
}