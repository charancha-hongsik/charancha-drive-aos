package com.milelog.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.milelog.DividerItemDecoration
import com.milelog.R
import com.milelog.retrofit.request.Place
import com.milelog.retrofit.response.GetDrivingGraphDataResponse
import com.milelog.room.entity.MyCarsEntity

class AllAddressActivity:BaseRefreshActivity() {
    lateinit var rv_all_address:RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_address)

        val type = object : TypeToken<MutableList<Place>>() {}.type
        val places: MutableList<Place> = GsonBuilder().serializeNulls().create().fromJson(intent.getStringExtra("places"), type)

        rv_all_address = findViewById(R.id.rv_all_address)

        rv_all_address.layoutManager = LinearLayoutManager(this)
        rv_all_address.adapter = AllAddressAdapter(context = this, places)

    }

    class AllAddressAdapter(
        private val context: Context,
        private val allPlaces: MutableList<Place>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_registered_bluetooth, parent, false)
            return MyCarEntitiesHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is MyCarEntitiesHolder) {
                val place = allPlaces[position]

                holder.tv_car_name.text = place.name + "\n" + place.address.parcel.name + "\n" + place.category

            }
        }

        override fun getItemCount(): Int {
            return allPlaces.size
        }
    }

    class MyCarEntitiesHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_car_name: TextView = view.findViewById(R.id.tv_car_name)
    }
}