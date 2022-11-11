
package com.example.lostarkupgradeapplication.material

import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.lostarkupgradeapplication.CustomToast
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //setContentView(R.layout.activity_material)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_material)
        binding.materialViewModel = viewModel
        title = "재료 수정"

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
            val toast = CustomToast(this)
            toast.createToast("재료값들이 저장되었습니다.", false)
            toast.show()
            CoroutineScope(Dispatchers.IO).launch {
                for (i in 0 until materials.size) {
                    if (materialRecyclerAdapter.saveState[i] != null) {
                        materials[i].count = materialRecyclerAdapter.saveState[i]!!
                        materialDao.update(materials[i])
                    }
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
            outRect.left = verticalSpaceHeight/2
            outRect.right = verticalSpaceHeight/2
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}