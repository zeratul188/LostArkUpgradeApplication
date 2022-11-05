package com.example.lostarkupgradeapplication.equipment

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.example.lostarkupgradeapplication.R
import com.example.lostarkupgradeapplication.databinding.DialogEditBinding
import com.example.lostarkupgradeapplication.db.UpgradeDBAdapter
import com.example.lostarkupgradeapplication.room.Equipment
import com.example.lostarkupgradeapplication.room.EquipmentDatabase
import com.jakewharton.rxbinding4.widget.itemSelections
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

    val name: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val level: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    init {
        name.value = "+${equipment.level} ${equipment.name}"
        level.value = equipment.itemlevel
    }

    fun setOnClickListener(listener: OnDialogClickListener) {
        onClickListener = listener
    }

    fun show() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_edit, null, false)
        dialog.setContentView(binding.root)

        with(binding) {
            changeTier(this)
            edtName.setText(equipment.name)
            val tiers = context.resources.getStringArray(R.array.tier)
            val tierAdapter = ArrayAdapter(context, R.layout.spr_item, tiers)
            sprTier.adapter = tierAdapter
            sprTier.setSelection(equipment.statue-1)
            setLevel(this)
            val sprTierChangeObservable = sprTier.itemSelections()
            val sprTierSubscription: Disposable = sprTierChangeObservable
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                    onNext = {
                        equipment.statue = it+1
                        changeTier(this)
                    },
                    onComplete = {

                    },
                    onError = {

                    }
                )
            myCompositeDisposable.add(sprTierSubscription)
        }

        binding.btnApply.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                dao?.update(equipment)
                handler.post {
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
        updateDBAdapter.close()
        val levels = Array<String>(range[1]-range[0]+1) { "" }
        for (i in levels.indices) {
            levels[i] = "${range[0]+i} 단계"
        }
        val levelAdapter = ArrayAdapter(context, R.layout.spr_item, levels)
        binding.sprLevel.adapter = levelAdapter
        val sprLevelObservable = binding.sprLevel.itemSelections()
        sprLevelSubscription = sprLevelObservable
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onNext = {
                    equipment.level = range[0] + it
                    name.value = "+${equipment.level} ${equipment.name}"
                },
                onComplete = {

                },
                onError = {

                }
            )
        myCompositeDisposable.add(sprLevelSubscription)
        binding.sprLevel.setSelection(0)
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