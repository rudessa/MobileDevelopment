package com.example.portraitlandscape

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var adapter: ArrayAdapter<CharSequence>
    private lateinit var imageView: ImageView
    private lateinit var spinner: Spinner
    private lateinit var prefs: SharedPreferences

    private val images = listOf(
        R.drawable.car1,
        R.drawable.car2,
        R.drawable.car3
    )

    private var currentImageIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.picture)
        spinner = findViewById(R.id.pictures_list)
        prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Load saved image index
        currentImageIndex = prefs.getInt("selected_image_index", 0)

        adapter = ArrayAdapter.createFromResource(this, R.array.pictures, R.layout.item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        // Restore state
        updateUI(currentImageIndex)
        spinner.setSelection(currentImageIndex)
    }

    private fun updateUI(index: Int) {
        imageView.setImageResource(images[index])
    }

    private fun saveSelection(index: Int) {
        currentImageIndex = index
        prefs.edit().putInt("selected_image_index", index).apply()
    }

    fun onChangePictureClick(v: View) {
        val nextIndex = (currentImageIndex + 1) % images.size
        saveSelection(nextIndex)
        updateUI(nextIndex)
        spinner.setSelection(nextIndex)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position != currentImageIndex) {
            saveSelection(position)
            updateUI(position)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Do nothing
    }
}
