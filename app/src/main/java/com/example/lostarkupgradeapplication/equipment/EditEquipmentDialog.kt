package com.example.lostarkupgradeapplication.equipment

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.example.lostarkupgradeapplication.CustomToast
import com.example.lostarkupgradeapplication.R
import com.example.lostarkupgradeapplication.databinding.DialogEditBinding
import com.example.lostarkupgradeapplication.db.UpgradeDBAdapter
import com.example.lostarkupgradeapplication.room.Equipment
import com.example.lostarkupgradeapplication.room.EquipmentDatabase
import com.jakewharton.rxbinding4.widget.itemSelections
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditEquipmentDialog(
    private val context: Context,
    private val equipment: Equipment,
    private val myCompositeDisposable: CompositeDisposable
) {
    private val dialog = Dialog(context)
    private lateinit var onClickListener: OnDialogClickListener
    private lateinit var binding: DialogEditBinding

    private val updateDBAdapter = UpgradeDBAdapter(context)
    private var sprLevelSubscription: Disposable? = null
    private val database = EquipmentDatabase.getInstance(context)
    private val dao = database?.equipmentDao()
    private val handler = Handler()

    fun setOnClickListener(listener: OnDialogClickListener) {
        onClickListener = listener
    }

    fun show() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_edit, null, false)
        dialog.setContentView(binding.root)

        with(binding) {
            txtName.text = "+${equipment.level} ${equipment.name}"
            txtLevel.text = "Lv.${equipment.itemlevel}"
            changeTier(this)
            edtName.setText(equipment.name)
            val tiers = context.resources.getStringArray(R.array.tier)
            val tierAdapter = ArrayAdapter(context, R.layout.spr_item, tiers)
            sprTier.adapter = tierAdapter
            sprTier.setSelection(equipment.statue-1)
            setLevel(this)
            var outType = equipment.type
            if (outType != "무기") {
                outType = "방어구"
            }
            updateDBAdapter.open()
            val checkLevel = updateDBAdapter.getRange(outType, equipment.statue)[0]
            updateDBAdapter.close()
            sprLevel.setSelection(equipment.level-checkLevel)
            println("Index : ${equipment.level-checkLevel}")
            val sprTierChangeObservable = sprTier.itemSelections()
            val sprTierSubscription: Disposable = sprTierChangeObservable
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        equipment.statue = it+1
                        changeTier(this)
                    },
                    onComplete = {

                    },
                    onError = {
                        Log.d("RXError", "Error : $it")
                        it.printStackTrace()
                    }
                )
            myCompositeDisposable.add(sprTierSubscription)
        }

        binding.btnApply.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                equipment.name = binding.edtName.text.toString()
                dao?.update(equipment)
                handler.post {
                    onClickListener.onClicked()
                    val toast = CustomToast(context)
                    toast.createToast("장비의 정보가 수정되었습니다.", false)
                    toast.show()
                    dialog.dismiss()
                }
            }
        }

        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()
    }

    fun setLevel(binding: DialogEditBinding) {
        if (sprLevelSubscription != null) {
            myCompositeDisposable.remove(sprLevelSubscription)
        }
        var outType = equipment.type
        if (outType != "무기") {
            outType = "방어구"
        }
        updateDBAdapter.open()
        val range = updateDBAdapter.getRange(outType, equipment.statue)
        range[0] -= 1
        updateDBAdapter.close()
        val levels = Array<String>(range[1]-range[0]+1) { "" }
        for (i in levels.indices) {
            levels[i] = "${range[0]+i} 단계"
        }
        val levelAdapter = ArrayAdapter(context, R.layout.spr_item, levels)
        binding.sprLevel.adapter = levelAdapter
        val sprLevelObservable = binding.sprLevel.itemSelections()
        sprLevelSubscription = sprLevelObservable
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    equipment.level = range[0] + it
                    binding.txtName.text = "+${equipment.level} ${equipment.name}"
                    updateDBAdapter.open()
                    var iLevel = updateDBAdapter.getLevel(outType, equipment.statue, equipment.level)
                    updateDBAdapter.close()
                    if (iLevel == -1) {
                        iLevel = when(equipment.statue) {
                            1 -> 1325
                            2 -> 1370
                            3 -> 1500
                            4 -> 1570
                            else -> {-1}
                        }
                    }
                    binding.txtLevel.text = "Lv.${equipment.itemlevel}"
                    equipment.itemlevel = iLevel
                },
                onComplete = {

                },
                onError = {
                    Log.d("RXError", "Error : $it")
                    it.printStackTrace()
                }
            )
        myCompositeDisposable.add(sprLevelSubscription)
        //binding.sprLevel.setSelection(0)
        updateDBAdapter.open()
        var iLevel = updateDBAdapter.getLevel(outType, equipment.statue, equipment.level)
        println("Item Level : ${iLevel}")
        updateDBAdapter.close()
        if (iLevel == -1) {
            iLevel = when(equipment.statue) {
                1 -> 1325
                2 -> 1370
                3 -> 1500
                4 -> 1570
                else -> {-1}
            }
        }
        binding.txtLevel.text = "Lv.${equipment.itemlevel}"
        equipment.itemlevel = iLevel
    }

    fun changeTier(binding: DialogEditBinding) {
        with(binding) {
            val equips = context.resources.getStringArray(R.array.type)
            val equipPosition = equips.indexOf(equipment.type)+1
            imgEquip.setImageResource(context.resources.getIdentifier("eq${equipPosition}_${equipment.statue}", "drawable", context.packageName))
            when(equipment.statue) {
                1 -> {
                    txtName.setTextColor(context.resources.getColor(R.color.grade_abv_end))
                    imgEquip.setBackgroundResource(R.drawable.background_adv)
                }
                2 -> {
                    txtName.setTextColor(context.resources.getColor(R.color.grade_hero_end))
                    imgEquip.setBackgroundResource(R.drawable.background_hero)
                }
                3 -> {
                    txtName.setTextColor(context.resources.getColor(R.color.grade_relics_end))
                    imgEquip.setBackgroundResource(R.drawable.background_relics)
                }
                4 -> {
                    txtName.setTextColor(context.resources.getColor(R.color.grade_ancient_end))
                    imgEquip.setBackgroundResource(R.drawable.background_ancient)
                }
                else -> txtName.setTextColor(context.resources.getColor(R.color.text))
            }
            setLevel(this)
        }
    }

    interface OnDialogClickListener {
        fun onClicked()
    }
}