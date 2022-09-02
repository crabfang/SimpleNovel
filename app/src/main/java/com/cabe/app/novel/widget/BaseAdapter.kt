package com.cabe.app.novel.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T>(val context: Context): RecyclerView.Adapter<BaseViewHolder<T>>() {
    private var dataList: MutableList<T>?= null
    var onItemClick: ((data: T) -> Unit)?= null
    var onItemLongClick: ((data: T) -> Boolean)?= null
    fun setData(list: List<T>?) {
        dataList?.clear()
        list?.let {
            dataList = it.toMutableList()
        }
        notifyDataSetChanged()
    }
    fun addData(list: List<T>?) {
        if (dataList == null) {
            dataList = ArrayList()
        }
        if (list != null) {
            dataList?.addAll(list)
        }
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return dataList?.size ?: 0
    }
    fun getItemData(index: Int): T? {
        return if (index < 0 || index >= itemCount) null else dataList?.get(index)
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.onItemClick = onItemClick
        holder.onItemLongClick = onItemLongClick

        val itemData = getItemData(position) ?: return
        holder.bindViewHolder(itemData)
    }
}

abstract class BaseViewHolder<T>(itemView: View): RecyclerView.ViewHolder(itemView) {
    var onItemClick: ((data: T) -> Unit)?= null
    var onItemLongClick: ((data: T) -> Boolean)?= null
    fun bindViewHolder(data: T) {
        if(onItemClick != null) itemView.setOnClickListener { onItemClick?.invoke(data) }
        if(onItemLongClick != null) itemView.setOnLongClickListener { onItemLongClick?.invoke(data) ?: false }
        onBindData(data)
    }
    protected abstract fun onBindData(data: T)
}