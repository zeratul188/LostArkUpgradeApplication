package com.example.lostarkupgradeapplication.material

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lostarkupgradeapplication.App
import com.example.lostarkupgradeapplication.R
import com.example.lostarkupgradeapplication.databinding.ItemMaterialBinding
import com.example.lostarkupgradeapplication.room.Material
import com.example.lostarkupgradeapplication.room.MaterialDao
import com.example.lostarkupgradeapplication.room.MaterialDatabase
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MaterialRecyclerAdapter(
    private val items: ArrayList<Material>,
    private val context: Context,
    private val myCompositeDisposable: CompositeDisposable
) : RecyclerView.Adapter<MaterialRecyclerAdapter.ViewHolder>() {
    val saveState = HashMap<Int, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMaterialBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, context, myCompositeDisposable, saveState)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.apply {
            bind(item, position)
            itemView.tag = item
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        private val binding: ItemMaterialBinding,
        private val context: Context,
        private val myCompositeDisposable: CompositeDisposable,
        private val saveState: HashMap<Int, Int>
    ) : RecyclerView.ViewHolder(binding.root) {
        private val materialDB: MaterialDatabase = MaterialDatabase.getInstance(context)!!
        private val materialDao: MaterialDao = materialDB?.materialDao()!!

        fun bind(item: Material, position: Int) {
            with(binding) {
                val list = App.context().resources.getStringArray(R.array.names)
                txtName.text = list[item.uid-1]
                txtCount.text = item.count.toString()
                //edtCount.addTextChangedListener(null)
                edtCount.removeTextChangedListener(CountTextWatcher(position))
                edtCount.addTextChangedListener(CountTextWatcher(position))
                if (saveState.keys.indexOf(position) != -1) {
                    edtCount.setText(saveState[position].toString())
                    println("saveStat($position, ${saveState[position]}) : ${list[item.uid-1]}")
                } else {
                    edtCount.setText("")
                }
                edtCount.addTextChangedListener(CountTextWatcher(position))
                when(item.type) {
                    "파편" -> {
                        imgIcon.setImageResource(R.drawable.power)
                    }
                    "파괴" -> {
                        var downf = item.tier
                        if (downf == 2) {
                            downf = 1
                        }
                        imgIcon.setImageResource(App.context().resources.getIdentifier("up1_${downf}", "drawable", App.context().packageName))
                    }
                    "수호" -> {
                        var downf = item.tier
                        if (downf == 2) {
                            downf = 1
                        }
                        imgIcon.setImageResource(App.context().resources.getIdentifier("up2_${downf}", "drawable", App.context().packageName))
                    }
                    "돌파석" -> {
                        imgIcon.setImageResource(App.context().resources.getIdentifier("stone${item.tier}", "drawable", App.context().packageName))
                    }
                    "융합재료" -> {
                        imgIcon.setImageResource(App.context().resources.getIdentifier("fusion${item.tier}", "drawable", App.context().packageName))
                    }
                    "골드" -> {
                        imgIcon.setImageResource(R.drawable.gold)
                    }
                    "태양" -> {
                        imgIcon.setImageResource(App.context().resources.getIdentifier("sun${item.tier}", "drawable", App.context().packageName))
                    }
                }

                /*val edtCountChangeObservable = edtCount.textChanges()
                val edtCountSubscription: Disposable = edtCountChangeObservable
                    .debounce(500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(
                        onNext = {
                            if (it.toString() != "") {
                                //item.count = it.toString().toInt()
                                saveState[position] = it.toString().toInt()
                                Log.d("Change Count", "change value (${list[item.uid-1]}) : ${it.toString().toInt()}")
                            }
                        },
                        onComplete = {

                        },
                        onError = {
                            Log.d("RXError", "Error(Type : ${item.type}) : $it")
                            it.printStackTrace()
                        }
                    )
                myCompositeDisposable.add(edtCountSubscription)*/
            }
        }

        inner class CountTextWatcher(private val position: Int) : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString() != "") {
                    saveState[position] = p0.toString().toInt()
                    Log.d("data saved", "Saved($position) : ${p0.toString().toInt()}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        }
    }
}