
package com.example.lostarkupgradeapplication.material

import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.lostarkupgradeapplication.R
import com.example.lostarkupgradeapplication.databinding.ActivityMaterialBinding
import com.example.lostarkupgradeapplication.room.Material
import com.example.lostarkupgradeapplication.room.MaterialDao
import com.example.lostarkupgradeapplication.room.MaterialDatabase
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MaterialActivity : AppCompatActivity() {
    private val viewModel: MaterialViewModel by viewModels()
    private lateinit var binding: ActivityMaterialBinding

    private var myCompositeDisposable = CompositeDisposable()
    private lateinit var materialDB: MaterialDatabase
    private lateinit var materialDao: MaterialDao
    private val handler = Handler()

    private var materials = ArrayList<Material>()
    private lateinit var materialRecyclerAdapter: MaterialRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_material)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_material)
        binding.materialViewModel = viewModel

        materialDB = MaterialDatabase.getInstance(this)!!
        materialDao = materialDB?.materialDao()!!
        CoroutineScope(Dispatchers.IO).launch {
            materials = materialDao.getAll() as ArrayList<Material>
            materialRecyclerAdapter = MaterialRecyclerAdapter(materials, this@MaterialActivity, myCompositeDisposable)
            handler.post {
                binding.listView.adapter = materialRecyclerAdapter
                val spaceDecoration = VerticalSpaceItemDecoration(20)
                binding.listView.addItemDecoration(spaceDecoration)
            }
        }

        binding.btnApply.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                materials.forEach { material ->
                    materialDao.update(material)
                }
                finish()
            }
        }
    }

    override fun onDestroy() {
        myCompositeDisposable.clear()
        super.onDestroy()
    }

    inner class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int): RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = verticalSpaceHeight
        }
    }
}