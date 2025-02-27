package com.lovelive.dreamycolor.LoveLiveResource.ViewHolder

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class LoveLiveBaseViewHolder<T>(itemView: View): RecyclerView.ViewHolder(itemView) {

    abstract fun bind(data: T, position: Int)

}