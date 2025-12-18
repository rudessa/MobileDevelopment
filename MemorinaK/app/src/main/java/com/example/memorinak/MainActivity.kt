package com.example.memorinak

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private var firstCard: ImageView? = null
    private var openCardsCount = 0
    private var totalPairsFound = 0
    private val totalPairs = 8

    private var startTime = 0L
    private var isTimerRunning = false
    private lateinit var tvTimer: TextView
    private lateinit var tvBestTime: TextView
    private lateinit var boardLayout: LinearLayout

    private val animals = listOf("bull", "cat", "cow", "dog", "donkey", "goat", "horse", "pig")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        val controlsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(20, 20, 20, 20)
        }

        tvTimer = TextView(this).apply {
            text = "Время: 0"
            textSize = 18f
        }

        tvBestTime = TextView(this).apply {
            val best = getBestTime()
            text = "Рекорд: ${if (best == Long.MAX_VALUE) "-" else "$best сек"}"
            setPadding(40, 0, 40, 0)
            textSize = 18f
        }

        val btnRestart = Button(this).apply {
            text = "Рестарт"
            setOnClickListener { restartGame() }
        }

        controlsLayout.addView(tvTimer)
        controlsLayout.addView(tvBestTime)
        controlsLayout.addView(btnRestart)
        rootLayout.addView(controlsLayout)

        boardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        rootLayout.addView(boardLayout)
        setContentView(rootLayout)

        setupBoard()
        startTimerLoop()
    }

    private fun setupBoard() {
        boardLayout.removeAllViews()
        val cardImages = (animals + animals).shuffled()
        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)

        val rows = Array(4) { LinearLayout(this) }

        for (i in 0 until 16) {
            val iv = ImageView(this).apply {
                setImageResource(R.drawable.sidecard)
                layoutParams = params
                setPadding(10, 10, 10, 10)
                tag = cardImages[i]
                setOnClickListener { cardClickListener(it) }
            }
            rows[i / 4].addView(iv)
        }

        for (row in rows) {
            row.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            boardLayout.addView(row)
        }
    }

    private fun cardClickListener(view: View) {
        val currentCard = view as ImageView

        if (!isTimerRunning && totalPairsFound < totalPairs) {
            startTime = System.currentTimeMillis()
            isTimerRunning = true
        }

        MainScope().launch {
            when (openCardsCount) {
                0 -> {
                    showFace(currentCard)
                    firstCard = currentCard
                    openCardsCount = 1
                }
                1 -> {
                    if (currentCard == firstCard) return@launch

                    showFace(currentCard)
                    openCardsCount = 2

                    delay(800)

                    if (currentCard.tag == firstCard?.tag) {
                        currentCard.visibility = View.INVISIBLE
                        firstCard?.visibility = View.INVISIBLE
                        totalPairsFound++
                        checkWin()
                    } else {
                        currentCard.setImageResource(R.drawable.sidecard)
                        firstCard?.setImageResource(R.drawable.sidecard)
                        currentCard.isClickable = true
                        firstCard?.isClickable = true
                    }
                    firstCard = null
                    openCardsCount = 0
                }
            }
        }
    }

    private fun showFace(iv: ImageView) {
        val resId = resources.getIdentifier(iv.tag as String, "drawable", packageName)
        iv.setImageResource(resId)
        iv.isClickable = false
    }

    private fun checkWin() {
        if (totalPairsFound == totalPairs) {
            isTimerRunning = false
            val finalTime = (System.currentTimeMillis() - startTime) / 1000
            Toast.makeText(this, "Победа! Время: $finalTime сек.", Toast.LENGTH_LONG).show()
            saveBestTime(finalTime)
            tvBestTime.text = "Рекорд: ${getBestTime()} сек"
        }
    }

    private fun restartGame() {
        isTimerRunning = false
        totalPairsFound = 0
        openCardsCount = 0
        firstCard = null
        tvTimer.text = "Время: 0"

        setupBoard()
    }

    private fun startTimerLoop() {
        MainScope().launch {
            while (true) {
                if (isTimerRunning) {
                    val seconds = (System.currentTimeMillis() - startTime) / 1000
                    tvTimer.text = "Время: $seconds"
                }
                delay(500)
            }
        }
    }

    private fun saveBestTime(time: Long) {
        val prefs = getSharedPreferences("game_stats", Context.MODE_PRIVATE)
        val currentBest = prefs.getLong("best_time", Long.MAX_VALUE)
        if (time < currentBest) {
            prefs.edit().putLong("best_time", time).apply()
        }
    }

    private fun getBestTime(): Long {
        return getSharedPreferences("game_stats", Context.MODE_PRIVATE)
            .getLong("best_time", Long.MAX_VALUE)
    }
}