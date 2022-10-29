package com.example.lostarkupgradeapplication

import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.lostarkupgradeapplication.databinding.ActivityMainBinding
import com.example.lostarkupgradeapplication.room.Equipment
import com.example.lostarkupgradeapplication.room.EquipmentDao
import com.example.lostarkupgradeapplication.room.EquipmentDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private lateinit var db: EquipmentDatabase
    private lateinit var dao: EquipmentDao
    private var items = ArrayList<Equipment>()
    private lateinit var equipmentAdapter: EquipmentRecyclerAdapter

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.mainViewModel = viewModel

        db = EquipmentDatabase.getInstance(this)!!
        dao = db?.equipmentDao()!!

        CoroutineScope(Dispatchers.IO).launch {
            items = (dao?.getAll() as ArrayList<Equipment>?)!!
            equipmentAdapter = EquipmentRecyclerAdapter(items, applicationContext)
            binding.listView.adapter = equipmentAdapter
            val spaceDecoration = VerticalSpaceItemDecoration(20)
            binding.listView.addItemDecoration(spaceDecoration)
        }

        initData()
    }

    // 초기값 설정
    private fun initData() {
        CoroutineScope(Dispatchers.IO).launch {
            var list = dao?.getAll()!!
            if (list.isEmpty()) {
                val types = resources.getStringArray(R.array.type)
                var index = 1
                println(types.size)
                types.forEach {
                    val item = Equipment(index, "기본 $it", it, 6, 1325, 0.0, 0, 1, 0)
                    index++
                    db?.equipmentDao()?.insertAll(item)
                    println("Added item $it ##################")
                }
                list = dao?.getAll()!!
                items.clear()
                items.addAll(list)
                handler.post {
                    equipmentAdapter.notifyDataSetChanged()
                }
            }
        }
    }
    /*
     * type : 무기, 투구 등등
     * level : 6
     * item_level : 1325
     * power : 0
     * honer : 0
     * statue : 1
     * stack : 0
     */

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            items.clear()
            val list = dao?.getAll()!!
            items.addAll(list)
            handler.post {
                equipmentAdapter.notifyDataSetChanged()
            }
        }
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