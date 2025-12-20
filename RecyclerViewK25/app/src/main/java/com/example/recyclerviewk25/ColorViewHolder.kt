package com.example.recyclerviewk25

import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tv = itemView.findViewById<TextView>(R.id.color)
    private var currentColor: Int = 0

    init {
        itemView.setOnClickListener {
            val context = itemView.context
            val colorHex = context.getString(R.string.template, currentColor)
            Toast.makeText(context, "Цвет: $colorHex", Toast.LENGTH_SHORT).show()
        }
    }

    fun bindTo(color: Int) {
        currentColor = color
        tv.setBackgroundColor(color)
        tv.text = itemView.context.getString(R.string.template, color)
    }
}