package com.example.lostarkupgradeapplication.upgrade

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.lostarkupgradeapplication.R
import com.example.lostarkupgradeapplication.databinding.ActivityUpgradeBinding
import com.example.lostarkupgradeapplication.room.Equipment
import com.example.lostarkupgradeapplication.room.EquipmentDao
import com.example.lostarkupgradeapplication.room.EquipmentDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpgradeActivity : AppCompatActivity() {
    private val viewModel: UpgradeViewModel by viewModels()
    private lateinit var binding: ActivityUpgradeBinding

    private var type = "none"
    private var equipment = Equipment(0, "none", "none", 0, 0, 0.0, 0, 1, 0)

    private lateinit var db: EquipmentDatabase
    private lateinit var dao: EquipmentDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_upgrade)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade)
        binding.upgradeViewModel = viewModel

        val intent = intent
        type = intent.getStringExtra("type").toString()

        db = EquipmentDatabase.getInstance(this)!!
        dao = db?.equipmentDao()!!

        CoroutineScope(Dispatchers.IO).launch {
            equipment = dao?.findByType(type)!!
            runOnUiThread {
                viewModel.equipment.value = equipment
                with(binding) {
                    val equips = resources.getStringArray(R.array.type)
                    val equip_position = equips.indexOf(equipment.type)+1
                    imgEquip.setImageResource(resources.getIdentifier("eq${equip_position}_${equipment.statue}", "drawable", packageName))
                }
            }
        }
    }
}