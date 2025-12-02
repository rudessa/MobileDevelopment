package com.example.guessthenumber

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    private var begin: Int = 0
    private var end: Int = 100
    private lateinit var tvQuestion: TextView
    private lateinit var btnYes: Button
    private lateinit var btnNo: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        begin = intent.getIntExtra("begin", 0)
        end = intent.getIntExtra("end", 100)
        tvQuestion = findViewById(R.id.question)
        btnYes = findViewById(R.id.yes)
        btnNo = findViewById(R.id.no)
        askQuestion()
    }

    private fun askQuestion() {
        if (begin == end) {
            tvQuestion.text = "Ваше число $begin!"
            Toast.makeText(this, "Я угадал! Это $begin", Toast.LENGTH_LONG).show()
            btnYes.visibility = View.GONE
            btnNo.text = "Заново"
        } else {
            val mid = begin + (end - begin) / 2
            tvQuestion.text = "Ваше число больше $mid?"
        }
    }

    fun onYesNoClick(view: View) {
        if (btnNo.text == "Заново") {
            finish()
            return
        }

        val mid = begin + (end - begin) / 2
        if (view.id == R.id.yes) {
            begin = mid + 1
        } else {
            end = mid
        }
        askQuestion()
    }
}
