package com.example.guessthenumber

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onGuessClick(view: View) {
        val etBegin = findViewById<EditText>(R.id.begin)
        val etEnd = findViewById<EditText>(R.id.end)

        val beginText = etBegin.text.toString()
        val endText = etEnd.text.toString()

        if (beginText.isEmpty() || endText.isEmpty()) {
            Toast.makeText(this, "Заполните оба поля!", Toast.LENGTH_SHORT).show()
            return
        }

        val begin = beginText.toIntOrNull()
        val end = endText.toIntOrNull()

        if (begin == null || end == null) {
            Toast.makeText(this, "Введите корректные числа!", Toast.LENGTH_SHORT).show()
            return
        }

        if (begin >= end) {
            Toast.makeText(this, "Начало должно быть меньше конца!", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("begin", begin)
        intent.putExtra("end", end)
        startActivity(intent)
    }
}