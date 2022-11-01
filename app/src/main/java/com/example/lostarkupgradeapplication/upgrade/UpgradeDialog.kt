package com.example.lostarkupgradeapplication.upgrade

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.lostarkupgradeapplication.CustomToast
import com.example.lostarkupgradeapplication.R
import com.example.lostarkupgradeapplication.databinding.DialogUgpradeBinding
import com.example.lostarkupgradeapplication.room.Equipment
import com.example.lostarkupgradeapplication.room.EquipmentDao
import com.example.lostarkupgradeapplication.room.MaterialDao
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
import java.lang.Math.floor

class UpgradeDialog(
    private val context: Context,
    private val materialDao: MaterialDao,
    private val upgrade: Upgrade,
    private val equipment: Equipment,
    private val equipmentDao: EquipmentDao,
    private val myCompositeDisposable: CompositeDisposable,
    private val activity: UpgradeActivity
) {
    private val dialog = Dialog(context)
    private lateinit var onClickListener: OnDialogClickListener
    private lateinit var binding: DialogUgpradeBinding

    private val handler = Handler()
    private var isBook = false

    val percent: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }
    val power = 0
    val plusPower: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }
    val suns =  Array<Int>(3) { 0 }

    init {
        percent.value = 0.0
        plusPower.value = 0.0
    }

    fun setOnClickListener(listener: OnDialogClickListener) {
        onClickListener = listener
    }

    fun show() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_ugprade, null, false)
        dialog.setContentView(binding.root)

        with(binding) {
            txtBefore.text = "${equipment.level} 단계"
            txtAfter.text = "${equipment.level+1} 단계"
            /*if (equipment.power >= 100) {
                txtPercent.text = "100%"
                progressPercent.progress = 10000
            } else {
                txtPercent.text = "${upgrade.percent + (upgrade.percent * 0.1 * equipment.stack)}%"
            }*/

            initData()

            val percentObserver = Observer<Double> { data ->
                if (data >= 100) {
                    txtPercent.text = "100%"
                    progressPercent.progress = 10000
                } else {
                    txtPercent.text = "${data}%"
                    progressPercent.progress = (data*100).toInt()
                }
            }
            percent.observe(activity, percentObserver)
            val plusPowerObserver = Observer<Double> { data ->
                if (equipment.power >= 100) {
                    txtPower.text = "100%"
                } else {
                    txtPower.text = "${floor(equipment.power*100)/100}%(+${data}%)"
                }
            }
            plusPower.observe(activity, plusPowerObserver)

            CoroutineScope(Dispatchers.IO).launch {
                val sunMaterials = Array<Int>(3) { 0 }
                for (i in 1..3) {
                    val value = materialDao.findItem("태양", i)
                    sunMaterials[i-1] = value.count
                }

                val chkBookChangeObservable = chkBook.checkedChanges()
                val chkBookSubscription: Disposable = chkBookChangeObservable
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onNext = {
                            isBook = it
                            if (it) {
                                percent.value = percent.value!! + upgrade.book.toDouble()
                            } else {
                                percent.value = percent.value!! - upgrade.book.toDouble()
                            }
                            percent.value = floor(percent.value!! * 100) / 100
                            plusPower.value = floor(percent.value!! * 0.465 * 100) / 100
                        },
                        onComplete = {

                        },
                        onError = {
                            Log.d("RXError", "Error : $it")
                            it.printStackTrace()
                        }
                    )
                myCompositeDisposable.add(chkBookSubscription)

                val seekSun1ChangeObservable = seekSun1.changeEvents()
                val seekSun2ChangeObservable = seekSun2.changeEvents()
                val seekSun3ChangeObservable = seekSun3.changeEvents()
                val seekSung1Subscription: Disposable = seekSun1ChangeObservable
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onNext = {
                            if (seekSun1.progress > sunMaterials[0]) {
                                seekSun1.progress = sunMaterials[0]
                            }
                            setSun(1, seekSun1.progress)
                        },
                        onComplete = {

                        },
                        onError = {
                            Log.d("RXError", "Error (1) : $it")
                            it.printStackTrace()
                        }
                    )
                val seekSung2Subscription: Disposable = seekSun2ChangeObservable
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onNext = {
                            if (seekSun2.progress > sunMaterials[1]) {
                                seekSun2.progress = sunMaterials[1]
                            }
                            setSun(2, seekSun2.progress)
                        },
                        onComplete = {

                        },
                        onError = {
                            Log.d("RXError", "Error (2) : $it")
                            it.printStackTrace()
                        }
                    )
                val seekSung3Subscription: Disposable = seekSun3ChangeObservable
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onNext = {
                            if (seekSun3.progress > sunMaterials[2]) {
                                seekSun3.progress = sunMaterials[2]
                            }
                            setSun(3, seekSun3.progress)
                        },
                        onComplete = {

                        },
                        onError = {
                            Log.d("RXError", "Error (3) : $it")
                            it.printStackTrace()
                        }
                    )
                myCompositeDisposable.add(seekSung1Subscription)
                myCompositeDisposable.add(seekSung2Subscription)
                myCompositeDisposable.add(seekSung3Subscription)

                handler.post {
                    if (equipment.power >= 100) {
                        seekSun1.isEnabled = false
                        seekSun2.isEnabled = false
                        seekSun3.isEnabled = false
                        chkBook.isEnabled = false
                        percent.value = 100.0
                    }

                    txtSun1.text = "태양의 은총(${sunMaterials[0]}개)"
                    txtSunPercent1.text = "(1개당 ${upgrade.per_ad}% 상승)"
                    seekSun1.max = upgrade.ad
                    txtSunStatue1.text = "0 / ${upgrade.ad}"

                    txtSun2.text = "태양의 축복(${sunMaterials[1]}개)"
                    txtSunPercent2.text = "(1개당 ${upgrade.per_rr}% 상승)"
                    seekSun2.max = upgrade.rr
                    txtSunStatue2.text = "0 / ${upgrade.rr}"

                    txtSun3.text = "태양의 가호(${sunMaterials[2]}개)"
                    txtSunPercent3.text = "(1개당 ${upgrade.per_hr}% 상승)"
                    seekSun3.max = upgrade.hr
                    txtSunStatue3.text = "0 / ${upgrade.hr}"

                    if (upgrade.book == -1) {
                        chkBook.visibility = View.GONE
                    }
                    chkBook.text = "특수 재료 사용 (재련 확률 ${upgrade.book}% 상승)"
                }
            }
        }

        binding.btnUpgrade.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                var content = ""
                val random = (0..9999).random().toInt()
                if (random < (percent.value!!*100).toInt()) { //재련 성공
                    equipment.level++
                    equipment.stack = 0
                    equipment.power = 0.0
                    equipment.honer = 0
                    equipment.itemlevel = upgrade.item
                    content = "장비 재련에 성공하였습니다!! (${equipment.level-1} 단계 -> ${equipment.level} 단계)"
                } else { //재련 실패
                    if (equipment.stack < 10) {
                        equipment.stack++
                        val bonus = upgrade.percent/10
                        content = "장비 재련에 실패하였습니다. 다음 장비 재련시 ${bonus}%가 상승됩니다."
                    } else {
                        content = "장비 재련에 실패하였습니다. 더이상 장비 재련시 추가 상승이 적용되지 않습니다."
                    }
                    equipment.power += plusPower.value!!
                }
                equipmentDao.update(equipment)
                val material1 = materialDao.findItem("태양", 1)
                material1.count -= binding.seekSun1.progress
                materialDao.update(material1)
                val material2 = materialDao.findItem("태양", 2)
                material2.count -= binding.seekSun2.progress
                materialDao.update(material2)
                val material3 = materialDao.findItem("태양", 3)
                material3.count -= binding.seekSun3.progress
                materialDao.update(material3)
                onClickListener.onClicked()
                val toast = CustomToast(context)
                handler.post{
                    toast.createToast(content, false)
                    toast.show()
                    dialog.dismiss()
                }
            }
        }

        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()
    }

    fun setSun(index: Int, count: Int) {
        with(binding) {
            when(index) {
                1 -> {
                    txtSunStatue1.text = "$count / ${upgrade.ad}"
                    suns[0] = count
                }
                2 -> {
                    txtSunStatue2.text = "$count / ${upgrade.rr}"
                    suns[1] = count
                }
                3 -> {
                    txtSunStatue3.text = "$count / ${upgrade.hr}"
                    suns[2] = count
                }
            }
            val bonus = (upgrade.percent/10) * equipment.stack
            if (equipment.power >= 100) {
                percent.value = 100.0
            } else {
                percent.value = upgrade.percent + bonus + (upgrade.per_ad * suns[0]) + (upgrade.per_rr * suns[1]) + (upgrade.per_hr * suns[2])
                if (isBook) {
                    percent.value = percent.value!! + upgrade.book.toDouble()
                }
                percent.value = floor(percent.value!! * 100) / 100
                plusPower.value = floor(percent.value!! * 0.465 * 100) / 100
            }
        }
    }

    fun initData() {
        val bonus = (upgrade.percent/10) * equipment.stack
        if (equipment.power >= 100) {
            percent.value = 100.0
        } else {
            percent.value = upgrade.percent + bonus
            percent.value = floor(percent.value!! * 100) / 100
            plusPower.value = floor(percent.value!! * 0.465 * 100) / 100
        }
    }

    interface OnDialogClickListener {
        fun onClicked()
    }
}