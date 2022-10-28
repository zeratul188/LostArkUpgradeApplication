package com.example.lostarkupgradeapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lostarkupgradeapplication.databinding.ItemEquipBinding
import com.example.lostarkupgradeapplication.room.Equipment

class EquipmentRecyclerAdapter(
    private val items: ArrayList<Equipment>
) : RecyclerView.Adapter<EquipmentRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEquipBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.apply {
            bind(item)
            itemView.tag = item
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemEquipBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Equipment) {
            with(binding) {

                executePendingBindings()
            }
        }
    }
}