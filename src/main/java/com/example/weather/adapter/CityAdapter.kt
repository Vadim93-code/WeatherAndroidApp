package com.example.weather.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.FavoriteCityDeteils
import com.example.weather.R
import com.example.weather.databinding.SityCardBinding


class CityAdapter: RecyclerView.Adapter<CityAdapter.ItemHolder>() {

    var itemList = mutableListOf<CityItem>()

    class ItemHolder(item: View): RecyclerView.ViewHolder(item) {

        val binding = SityCardBinding.bind(item)

        fun bind(item: CityItem) = with(binding){
            textView5.text = item.cityName

            cityCard.setOnClickListener {
                val intent = Intent(textView5.context, FavoriteCityDeteils::class.java)
                intent.putExtra("CityName", item.cityName)
                intent.putExtra("Lat", item.lat)
                intent.putExtra("Lon", item.lon)
                textView5.context.startActivity(intent)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sity_card, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun addItem(item: CityItem){
        itemList.add(item)
        notifyDataSetChanged()
    }


}