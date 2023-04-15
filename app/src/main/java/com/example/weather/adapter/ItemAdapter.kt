package com.example.weather.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.databinding.WezerCardBinding


class ItemAdapter: RecyclerView.Adapter<ItemAdapter.ItemHolder>() {

    var itemList = mutableListOf<Item>()

    class ItemHolder(item: View): RecyclerView.ViewHolder(item) {

        val binding = WezerCardBinding.bind(item)

        fun bind(item: Item) = with(binding){
            textView.text = item.day
            textView2.text = item.status
            textView3.text = item.temp_min
            textView4.text = item.temp_max

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.wezer_card, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun addItem(item: Item){
        itemList.add(item)
        notifyDataSetChanged()
    }


}