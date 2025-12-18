package com.example.memorinak

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private val animals = listOf(
        "bull", "cat", "cow", "dog",
        "donkey", "goat", "horse", "pig"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        val cardImages = (animals + animals).shuffled()

        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
        params.weight = 1f

        val cardViews = ArrayList<ImageView>()

        for (i in 0 until 16) {
            val card = ImageView(this).apply {
                setImageResource(R.drawable.sidecard)
                layoutParams = params
                setPadding(5, 5, 5, 5)
                tag = cardImages[i]
                setOnClickListener(colorListener)
            }
            cardViews.add(card)
        }

        val rows = Array(4) { LinearLayout(this) }
        var count = 0
        for (view in cardViews) {
            val rowIndex = count / 4
            rows[rowIndex].addView(view)
            count++
        }

        for (row in rows) {
            row.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
            layout.addView(row)
        }

        setContentView(layout)
    }

    private suspend fun flipCard(v: View) {
        val imageView = v as ImageView
        val imageName = v.tag as String

        val resId = resources.getIdentifier(imageName, "drawable", packageName)

        imageView.setImageResource(resId)
        imageView.isClickable = false

        delay(1000)

        imageView.setImageResource(R.drawable.sidecard)
        imageView.isClickable = true
    }

    private val colorListener = View.OnClickListener {
        MainScope().launch {
            flipCard(it)
        }
    }
}