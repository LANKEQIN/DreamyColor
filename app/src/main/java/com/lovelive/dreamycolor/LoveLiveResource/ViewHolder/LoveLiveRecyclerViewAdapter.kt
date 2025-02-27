package com.lovelive.dreamycolor.LoveLiveResource.ViewHolder

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lovelive.dreamycolor.model.LoveLiveResourceCategoryModel

class LoveLiveRecyclerViewAdapter: RecyclerView.Adapter<LoveLiveBaseViewHolder<LoveLiveResourceCategoryModel>>() {

    private val mData = mutableListOf<LoveLiveResourceCategoryModel>()

    override fun onBindViewHolder(
        holder: LoveLiveBaseViewHolder<LoveLiveResourceCategoryModel>,
        position: Int
    ) {

        mData.getOrNull(position)?.let {
            holder.bind(it, position)
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LoveLiveBaseViewHolder<LoveLiveResourceCategoryModel> {
        return LoveLiveGridResourceViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return mData.size
    }
}