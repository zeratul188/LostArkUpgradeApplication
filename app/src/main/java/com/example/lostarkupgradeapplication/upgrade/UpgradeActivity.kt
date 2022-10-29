package com.example.lostarkupgradeapplication.upgrade

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.lostarkupgradeapplication.R
import com.example.lostarkupgradeapplication.databinding.ActivityUpgradeBinding
import com.example.lostarkupgradeapplication.db.UpgradeDBAdapter
import com.example.lostarkupgradeapplication.room.*
import com.example.lostarkupgradeapplication.upgrade.objects.Upgrade
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
    private lateinit var materialDB: MaterialDatabase
    private lateinit var materialDao: MaterialDao
    private lateinit var upgradeDBAdapter: UpgradeDBAdapter

    private lateinit var upgrade: Upgrade

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_upgrade)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade)
        binding.upgradeViewModel = viewModel

        val intent = intent
        type = intent.getStringExtra("type").toString()

        db = EquipmentDatabase.getInstance(this)!!
        dao = db?.equipmentDao()!!
        materialDB = MaterialDatabase.getInstance(this)!!
        materialDao = materialDB?.materialDao()!!

        upgradeDBAdapter = UpgradeDBAdapter(this)
        var outType = type
        if (outType != "무기") {
            outType = "방어구"
        }

        CoroutineScope(Dispatchers.IO).launch {
            equipment = dao?.findByType(type)!!
            upgrade = upgradeDBAdapter.getItem(outType, equipment.statue!!)!!
            val haveHoner = materialDao.findItem("파편", 0).count // 소지중인 명에의 파편
            val haveUp = materialDao.findItem("파괴", equipment.statue!!).count
            runOnUiThread {
                viewModel.equipment.value = equipment
                with(binding) {
                    val equips = resources.getStringArray(R.array.type)
                    val equip_position = equips.indexOf(equipment.type)+1
                    imgEquip.setImageResource(resources.getIdentifier("eq${equip_position}_${equipment.statue}", "drawable", packageName))

                    //명예의 파편 경험치
                    seekPower.max = upgrade.experience
                    seekPower.progress = equipment.honer!!
                    txtHonerCount.text = haveHoner.toString()
                    viewModel.maxSeek.value = upgrade.experience

                    //강화석
                    var upf = 1
                    if (outType != "무기") {
                        upf = 2
                    }
                    var downf = equipment.statue
                    if (downf == 2) {
                        downf = 1
                    }
                    imgUp.setImageResource(resources.getIdentifier("up${upf}_${downf}", "drawable", packageName))
                    txtUp.text = "${haveUp}\n/${upgrade.enforce}"

                    //돌파석
                }
            }
        }
    }
}