package com.milelog

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.recyclerview.widget.RecyclerView

class ImageSliderAdapter(var images: ArrayList<Int>) :
    RecyclerView.Adapter<ImageSliderAdapter.PagerViewHolder>() {


    inner class PagerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder
        (LayoutInflater.from(parent.context).inflate(R.layout.onboarding_item, parent, false)) {
        val item_onboarding_image = itemView.findViewById<ImageView>(R.id.iv_onboarding_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PagerViewHolder((parent))

    override fun getItemCount(): Int = Integer.MAX_VALUE

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        val realPosition = position % images.size
        holder.item_onboarding_image.setImageResource(images[realPosition])
        holder.item_onboarding_image.scaleType = ScaleType.CENTER_INSIDE

    }
}