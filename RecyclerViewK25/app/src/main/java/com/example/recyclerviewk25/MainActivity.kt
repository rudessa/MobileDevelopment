package com.example.recyclerviewk25

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.DefaultItemAnimator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rv = findViewById<RecyclerView>(R.id.rview)
        val colorAdapter = ColorAdapter(LayoutInflater.from(this))

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = colorAdapter

        rv.itemAnimator = DefaultItemAnimator()

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rv)

        val colorsList = generateMassivePalette(500)
        colorAdapter.submitList(colorsList)
    }

    private fun generateMassivePalette(count: Int): List<Int> {
        val colors = mutableListOf<Int>()
        val goldenRatio = 0.618033988749895f
        var h = (0..360).random().toFloat() / 360f

        for (i in 0 until count) {
            h = (h + goldenRatio) % 1.0f
            val s = if (i % 2 == 0) 0.5f else 0.8f
            val v = if (i % 3 == 0) 0.9f else 0.7f
            colors.add(Color.HSVToColor(floatArrayOf(h * 360f, s, v)))
        }
        return colors
    }
}