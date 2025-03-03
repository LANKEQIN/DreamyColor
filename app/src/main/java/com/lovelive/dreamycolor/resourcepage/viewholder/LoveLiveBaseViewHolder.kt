package com.lovelive.dreamycolor.resourcepage.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class LoveLiveBaseViewHolder<T>(itemView: View): RecyclerView.ViewHolder(itemView) {

    abstract fun bind(data: T, position: Int)

}