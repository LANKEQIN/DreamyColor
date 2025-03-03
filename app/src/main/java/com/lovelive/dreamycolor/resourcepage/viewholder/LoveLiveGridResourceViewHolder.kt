package com.lovelive.dreamycolor.resourcepage.viewholder

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import com.lovelive.dreamycolor.DreamyColorApplication
import com.lovelive.dreamycolor.R
import com.lovelive.dreamycolor.model.LoveLiveResourceCategoryModel

class LoveLiveGridResourceViewHolder(itemView: View): LoveLiveBaseViewHolder<LoveLiveResourceCategoryModel>(itemView) {

    companion object {

        const val SINGLE_LAYOUT_RESOURCE = 0

        const val DOUBLE_LAYOUT_RESOURCE = 1

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

        var rowCount = 0
        var columnCount = 0


        data.resourceList.forEachIndexed { index, loveLiveResourceModel ->
            val itemResourceLayout = LayoutInflater.from(resourceContainer.context).inflate(
                R.layout.lovelive_resource_container,
                resourceContainer
            )
            val resourceName = itemResourceLayout.findViewById<TextView>(R.id.resource_name)
            val resourceCover = itemResourceLayout.findViewById<ImageView>(R.id.resource_cover)
            val gridParams = GridLayout.LayoutParams()
            resourceName.text = loveLiveResourceModel.name
            itemResourceLayout.setOnClickListener {
                jumpToResource(loveLiveResourceModel.resourceUrl, it)
            }

            if (loveLiveResourceModel.type == SINGLE_LAYOUT_RESOURCE) {
                if(columnCount == 1) {
                    rowCount ++
                    columnCount = 0
                }
                gridParams.columnSpec = GridLayout.spec(columnCount, 2)
                gridParams.rowSpec = GridLayout.spec(rowCount,1)
                resourceContainer.addView(itemResourceLayout)
                rowCount++
                columnCount = 0
            } else {
                gridParams.columnSpec = GridLayout.spec(columnCount, 1)
                gridParams.rowSpec = GridLayout.spec(rowCount,1)
                resourceContainer.addView(itemResourceLayout)
                columnCount++
                if(columnCount > 1) {
                    rowCount ++
                    columnCount = 0
                }
            }
        }
    }

    fun jumpToResource(url: String, view: View) {
        val context = view.context ?: DreamyColorApplication.context
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}