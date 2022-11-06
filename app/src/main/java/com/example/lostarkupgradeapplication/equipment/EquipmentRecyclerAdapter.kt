package com.example.lostarkupgradeapplication.equipment

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lostarkupgradeapplication.App
import com.example.lostarkupgradeapplication.MainViewModel
import com.example.lostarkupgradeapplication.R
import com.example.lostarkupgradeapplication.databinding.ItemEquipBinding
import com.example.lostarkupgradeapplication.room.Equipment
import com.example.lostarkupgradeapplication.room.EquipmentDatabase
import com.example.lostarkupgradeapplication.upgrade.UpgradeActivity
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EquipmentRecyclerAdapter(
    private val items: ArrayList<Equipment>,
    private val context: Context,
    private val myCompositeDisposable: CompositeDisposable,
    private val viewModel: MainViewModel
) : RecyclerView.Adapter<EquipmentRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEquipBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, context, myCompositeDisposable)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.apply {
            bind(item)
            itemView.tag = item
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemEquipBinding, private val context: Context, private val myCompositeDisposable: CompositeDisposable) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Equipment) {
            with(binding) {
                equipment = item
                val equips = context.resources.getStringArray(R.array.type)
                val equip_position = equips.indexOf(item.type)+1
                imgEquip.setImageResource(context.resources.getIdentifier("eq${equip_position}_${item.statue}", "drawable", context.packageName))
                when(item.statue) {
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
                layoutMain.setOnClickListener {
                    val intent = Intent(App.context(), UpgradeActivity::class.java)
                    intent.putExtra("type", item.type)
                    App.context().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                btnSetting.setOnClickListener {
                    val dialog = EditEquipmentDialog(context, item, myCompositeDisposable)
                    dialog.setOnClickListener(object : EditEquipmentDialog.OnDialogClickListener {
                        override fun onClicked() {
                            val db = EquipmentDatabase.getInstance(context)!!
                            val dao = db?.equipmentDao()!!
                            CoroutineScope(Dispatchers.IO).launch {
                                val list = dao?.getAll()!!
                                var level = 0.0
                                items.clear()
                                items.addAll(list)
                                list.forEach { item ->
                                    level += item.itemlevel
                                }
                                level /= items.size
                                level = Math.floor(level * 100) /100
                                viewModel.allLevel.postValue(level)
                                println("Level : $level")
                            }
                            notifyDataSetChanged()
                        }
                    })
                    dialog.show()
                }
                executePendingBindings()
            }
        }
    }
}