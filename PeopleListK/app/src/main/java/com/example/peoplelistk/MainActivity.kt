package com.example.peoplelistk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView

class MainActivity : AppCompatActivity() {

    // MutableList
    private val people = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var lvPeople: ListView
    private lateinit var etName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lvPeople = findViewById(R.id.people)
        etName = findViewById(R.id.etName)

        val names = resources.getStringArray(R.array.names)
        val surnames = resources.getStringArray(R.array.surnames)

        for (i in 1..5) {
            people.add("${names.random()} ${surnames.random()}")
        }

        adapter = ArrayAdapter(this, R.layout.item, people)
        lvPeople.adapter = adapter

        lvPeople.choiceMode = ListView.CHOICE_MODE_SINGLE

        lvPeople.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            lvPeople.setItemChecked(position, true)
        }
    }

    fun onAddPersonClick(view: View) {
        val input = etName.text.toString()

        if (input.isNotBlank()) {
            people.add(input)
        } else {
            val names = resources.getStringArray(R.array.names)
            val surnames = resources.getStringArray(R.array.surnames)
            people.add("${names.random()} ${surnames.random()}")
        }

        adapter.notifyDataSetChanged()

        etName.text.clear()
    }
}