package com.example.lostarkupgradeapplication.upgrade

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.lostarkupgradeapplication.R
import com.example.lostarkupgradeapplication.databinding.ActivityUpgradeBinding
import com.example.lostarkupgradeapplication.db.TierupDBAdapter
import com.example.lostarkupgradeapplication.db.UpgradeDBAdapter
import com.example.lostarkupgradeapplication.room.*
import com.example.lostarkupgradeapplication.upgrade.objects.Tierup
import com.example.lostarkupgradeapplication.upgrade.objects.Upgrade
import com.jakewharton.rxbinding4.widget.changeEvents
import com.jakewharton.rxbinding4.widget.checkedChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
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
    private lateinit var tierupDBAdapter: TierupDBAdapter

    private lateinit var upgrade: Upgrade
    private val handler = Handler()
    private var isInfinity = false

    private var myCompositeDisposable = CompositeDisposable()

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
        tierupDBAdapter = TierupDBAdapter(this)

        val chkInfinityChangeObservable = binding.chkInfinity.checkedChanges()
        val chkInfinitySubscription: Disposable = chkInfinityChangeObservable
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    isInfinity = it
                },
                onComplete = {

                },
                onError = {
                    Log.d("RXError", "Error : $it")
                    it.printStackTrace()
                }
            )
        myCompositeDisposable.add(chkInfinitySubscription)

        val seekHonerChangeObservable = binding.seekPower.changeEvents()
        val seekHonerSubcription: Disposable = seekHonerChangeObservable
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val haveHoner = materialDao.findItem("파편", 0).count // 소지중인 명에의 파편
                        handler.post {
                            if (binding.seekPower.progress < equipment.honer!!) {
                                binding.seekPower.progress = equipment.honer!!
                            } else if (binding.seekPower.progress > haveHoner) {
                                binding.seekPower.progress = haveHoner
                                binding.btnApplyPower.isEnabled = false
                            } else if (equipment.honer == binding.seekPower.max) {
                                binding.btnApplyPower.isEnabled = false
                            } else {
                                viewModel.powerSeek.value = binding.seekPower.progress
                                binding.btnApplyPower.isEnabled = binding.seekPower.progress == binding.seekPower.max
                            }
                        }
                    }
                },
                onComplete = {

                },
                onError = {
                    Log.d("RXError", "Error : $it")
                    it.printStackTrace()
                }
            )
        myCompositeDisposable.add(seekHonerSubcription)

        val nowSeekObserver = Observer<Int> { data ->
            binding.txtPowerStatue.text = "${data}/${viewModel.maxSeek.value}"
        }
        viewModel.powerSeek.observe(this, nowSeekObserver)
        val maxSeekObserver = Observer<Int> { data ->
            binding.txtPowerStatue.text = "${viewModel.powerSeek.value}/$data"
        }
        viewModel.maxSeek.observe(this, maxSeekObserver)
        val equipmentObserver = Observer<Equipment> { data ->
            with(binding) {
                txtNowStatue.text = "${data.level} 단계"
                txtNowPlusStatue.text = "${data.level?.plus(1)} 단계"
                txtName.text = "+${data.level} ${data.name}"
                txtLevel.text = "Lv.${data.itemlevel}"
                txtPower.text = "${data.getProgress()}%"
            }
        }
        viewModel.equipment.observe(this, equipmentObserver)

        binding.btnTearUp.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                tierupDBAdapter.open()
                val afterStep = tierupDBAdapter.getItem(equipment.statue, equipment.level)?.after
                tierupDBAdapter.close()
                equipment.statue++
                if (afterStep != null) {
                    equipment.level = afterStep
                }
                reset()
                dao?.update(equipment)
                syncData()
            }
        }

        binding.btnApplyPower.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                var outType = type
                if (outType != "무기") {
                    outType = "방어구"
                }
                upgradeDBAdapter.open()
                upgrade = upgradeDBAdapter.getItem(outType, equipment.statue!!, equipment.level!!.plus(1))!!
                upgradeDBAdapter.close()
                val material = materialDao.findItem("파편", 0)
                material.count -= upgrade.experience
                materialDao.update(material)
                equipment.honer = upgrade.experience
                dao.update(equipment)
                handler.post {
                    checkHoner()
                    binding.btnApplyPower.isEnabled = false
                    binding.txtHoner.text = "${upgrade.fragments}/${material.count}"
                    if (upgrade.fragments > material.count) {
                        binding.txtHoner.setTextColor(resources.getColor(R.color.warning_text))
                    } else {
                        binding.txtHoner.setTextColor(resources.getColor(R.color.text))
                    }
                }
            }
        }

        syncData()
    }

    fun checkHoner() {
        var outType = type
        if (outType != "무기") {
            outType = "방어구"
        }
        upgradeDBAdapter.open()
        upgrade = upgradeDBAdapter.getItem(outType, equipment.statue!!, equipment.level!!.plus(1))!!
        upgradeDBAdapter.close()
        binding.btnUpgrade.isEnabled = upgrade.experience == equipment.honer
    }

    fun reset() {
        equipment.power = 0.0
        equipment.honer = 0
        equipment.stack = 0
    }

    fun checkTierup() {
        tierupDBAdapter.open()
        val tier = tierupDBAdapter.getItem(equipment.statue, equipment.level)
        tierupDBAdapter.close()
        binding.btnTearUp.isEnabled = tier != null
    }

    fun syncData() {
        var outType = type
        if (outType != "무기") {
            outType = "방어구"
        }
        CoroutineScope(Dispatchers.IO).launch {
            equipment = dao?.findByType(type)!!
            handler.post {
                checkTierup()
                checkHoner()
            }
            upgradeDBAdapter.open()
            upgrade = upgradeDBAdapter.getItem(outType, equipment.statue!!, equipment.level!!.plus(1))!!
            upgradeDBAdapter.close()
            val haveHoner = materialDao.findItem("파편", 0).count // 소지중인 명에의 파편
            var up = equipment.statue!!
            if (up == 2) {
                up = 1
            }
            val haveUp = materialDao.findItem("파괴", up).count
            val haveStone = materialDao.findItem("돌파석", equipment.statue!!).count
            val haveFusion = materialDao.findItem("융합재료", equipment.statue!!).count
            val haveGold = materialDao.findItem("골드", 0).count
            runOnUiThread {
                viewModel.equipment.value = equipment
                viewModel.powerSeek.value = equipment.honer
                with(binding) {
                    val equips = resources.getStringArray(R.array.type)
                    val equip_position = equips.indexOf(equipment.type)+1
                    imgEquip.setImageResource(resources.getIdentifier("eq${equip_position}_${equipment.statue}", "drawable", packageName))
                    when(equipment.statue) {
                        1 -> imgEquip.setBackgroundResource(R.drawable.background_adv)
                        2 -> imgEquip.setBackgroundResource(R.drawable.background_hero)
                        3 -> imgEquip.setBackgroundResource(R.drawable.background_relics)
                        4 -> imgEquip.setBackgroundResource(R.drawable.background_ancient)
                    }

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
                    txtUp.text = "${upgrade.enforce}\n/${haveUp}"
                    if (upgrade.enforce > haveUp) {
                        txtUp.setTextColor(resources.getColor(R.color.warning_text))
                    } else {
                        txtUp.setTextColor(resources.getColor(R.color.text))
                    }

                    //돌파석
                    imgStone.setImageResource(resources.getIdentifier("stone${equipment.statue}", "drawable", packageName))
                    txtStone.text = "${upgrade.stone}\n/${haveStone}"
                    if (upgrade.stone > haveStone) {
                        txtStone.setTextColor(resources.getColor(R.color.warning_text))
                    } else {
                        txtStone.setTextColor(resources.getColor(R.color.text))
                    }

                    //융합재료
                    imgFusion.setImageResource(resources.getIdentifier("fusion${equipment.statue}", "drawable", packageName))
                    txtFusion.text = "${upgrade.ingredient}\n/${haveFusion}"
                    if (upgrade.ingredient > haveFusion) {
                        txtFusion.setTextColor(resources.getColor(R.color.warning_text))
                    } else {
                        txtFusion.setTextColor(resources.getColor(R.color.text))
                    }

                    //명예의 파편, 골드
                    txtHoner.text = "${upgrade.fragments}/${haveHoner}"
                    if (upgrade.fragments > haveHoner) {
                        txtHoner.setTextColor(resources.getColor(R.color.warning_text))
                    } else {
                        txtHoner.setTextColor(resources.getColor(R.color.text))
                    }
                    txtGold.text = "${upgrade.gold}/${haveGold}"
                    if (upgrade.gold > haveGold) {
                        txtGold.setTextColor(resources.getColor(R.color.warning_text))
                    } else {
                        txtGold.setTextColor(resources.getColor(R.color.text))
                    }

                    executePendingBindings()
                }
            }
        }
    }

    override fun onDestroy() {
        myCompositeDisposable.clear()
        super.onDestroy()
    }
}