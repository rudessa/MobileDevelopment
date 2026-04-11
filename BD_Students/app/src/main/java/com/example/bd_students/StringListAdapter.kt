package com.example.bd_students

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StringListAdapter : RecyclerView.Adapter<StringListAdapter.StringViewHolder>() {

    private val items = mutableListOf<String>()

    fun submitList(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result, parent, false) as TextView
        return StringViewHolder(textView)
    }

    override fun onBindViewHolder(holder: StringViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class StringViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(text: String) {
            textView.text = text
        }
    }
}
