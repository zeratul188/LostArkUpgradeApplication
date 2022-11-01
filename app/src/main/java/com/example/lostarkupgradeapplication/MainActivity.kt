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
import com.example.lostarkupgradeapplication.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Math.floor

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private lateinit var db: EquipmentDatabase
    private lateinit var dao: EquipmentDao
    private lateinit var materialDB: MaterialDatabase
    private lateinit var materialDao: MaterialDao
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
        materialDB = MaterialDatabase.getInstance(this)!!
        materialDao = materialDB?.materialDao()!!

        CoroutineScope(Dispatchers.IO).launch {
            items = (dao?.getAll() as ArrayList<Equipment>?)!!
            equipmentAdapter = EquipmentRecyclerAdapter(items, applicationContext)
            handler.post {
                binding.listView.adapter = equipmentAdapter
                val spaceDecoration = VerticalSpaceItemDecoration(20)
                binding.listView.addItemDecoration(spaceDecoration)
            }
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
                var level = 0.0
                println(types.size)
                types.forEach {
                    val item = Equipment(index, "기본 $it", it, 6, 1325, 0.0, 0, 1, 0)
                    index++
                    db?.equipmentDao()?.insertAll(item)
                }
                list = dao?.getAll()!!
                items.clear()
                items.addAll(list)
                handler.post {
                    equipmentAdapter.notifyDataSetChanged()
                }
            }
            var materials = materialDao.getAll()
            if (materials.isEmpty()) {
                val mts = ArrayList<Material>()
                mts.add(Material(1, "파편", 0, 0))
                mts.add(Material(2, "파괴", 1, 0))
                mts.add(Material(3, "파괴", 3, 0))
                mts.add(Material(4, "파괴", 4, 0))
                mts.add(Material(5, "수호", 1, 0))
                mts.add(Material(6, "수호", 3, 0))
                mts.add(Material(7, "수호", 4, 0))
                for (i in 1..4) {
                    mts.add(Material(7+i, "돌파석", i, 0))
                }
                for (i in 1..4) {
                    mts.add(Material(11+i, "융합재료", i, 0))
                }
                mts.add(Material(16, "골드", 0, 0))
                for (i in 1..3) {
                    mts.add(Material(16+i, "태양", i, 0))
                }
                mts.forEach { material ->
                    materialDB.materialDao().insertAll(material)
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
            val list = dao?.getAll()!!
            var level = 0.0
            items.clear()
            items.addAll(list)
            list.forEach { item ->
                level += item.itemlevel
            }
            level /= items.size
            level = floor(level*100)/100
            handler.post {
                binding.txtAllLevel.text = "Lv.$level"
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