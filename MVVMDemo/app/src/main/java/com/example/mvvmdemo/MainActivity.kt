package com.example.mvvmdemo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val provider = ViewModelProvider(this)
        viewModel = provider.get(MainViewModel::class.java)

        observeViewModel()
        initView()
    }

    private fun observeViewModel() {
        val textCounter = findViewById<TextView>(R.id.text_counter)

        viewModel.counter.observe(this, Observer { count ->
            textCounter.text = count?.toString() ?: "0"
        })
    }

    private fun initView() {
        val btnIncrement = findViewById<Button>(R.id.btn_increment)

        btnIncrement.setOnClickListener {
            viewModel.onIncrementClicked()
        }
    }
}