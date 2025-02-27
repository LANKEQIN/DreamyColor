package com.lovelive.dreamycolor.LoveLiveResource.ViewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import com.lovelive.dreamycolor.R
import com.lovelive.dreamycolor.model.LoveLiveResourceCategoryModel

class LoveLiveGridResourceViewHolder(itemView: View): LoveLiveBaseViewHolder<LoveLiveResourceCategoryModel>(itemView) {

    companion object {
        fun create(parent: ViewGroup):LoveLiveGridResourceViewHolder {
            return LoveLiveGridResourceViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.lovelive_grid_resource_layout,
                    parent,
                    false
                )
            )
        }
    }

    private val title = itemView.findViewById<TextView>(R.id.lovelive_grid_title)

    private val resourceContainer = itemView.findViewById<GridLayout>(R.id.lovelive_grid_container)

    override fun bind(data: LoveLiveResourceCategoryModel, position: Int) {

        if (data.resourceCategoryName != "") {
            title.text = data.resourceCategoryName
        }



    }
}