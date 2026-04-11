package com.example.bd_students

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bd_students.data.AppDatabaseProvider
import com.example.bd_students.data.LookupMode
import com.example.bd_students.data.SchoolRepository
import com.example.bd_students.data.SelectableItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var modeRadioGroup: RadioGroup
    private lateinit var spinner: Spinner
    private lateinit var titleTextView: TextView
    private lateinit var emptyTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StringListAdapter
    private lateinit var repository: SchoolRepository

    private var currentMode: LookupMode = LookupMode.STUDENT
    private var currentItems: List<SelectableItem> = emptyList()
    private var suppressSpinnerCallback = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = SchoolRepository(AppDatabaseProvider.getDatabase(applicationContext).schoolDao())

        modeRadioGroup = findViewById(R.id.modeRadioGroup)
        spinner = findViewById(R.id.selectionSpinner)
        titleTextView = findViewById(R.id.tvResultTitle)
        emptyTextView = findViewById(R.id.tvEmpty)
        recyclerView = findViewById(R.id.resultsRecyclerView)

        adapter = StringListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        modeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbStudent -> if (currentMode != LookupMode.STUDENT) {
                    loadMode(LookupMode.STUDENT)
                }

                R.id.rbSubject -> if (currentMode != LookupMode.SUBJECT) {
                    loadMode(LookupMode.SUBJECT)
                }
            }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!suppressSpinnerCallback) {
                    loadResults(position)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        modeRadioGroup.check(R.id.rbStudent)
        loadMode(LookupMode.STUDENT)
    }

    private fun loadMode(mode: LookupMode) {
        lifecycleScope.launch {
            currentMode = mode
            titleTextView.text = getString(mode.titleResId)

            currentItems = withContext(Dispatchers.IO) {
                repository.getSelectableItems(mode)
            }

            val names = currentItems.map { it.title }
            suppressSpinnerCallback = true
            spinner.adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_item,
                names
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            suppressSpinnerCallback = false

            if (currentItems.isNotEmpty()) {
                spinner.setSelection(0)
                loadResults(0)
            } else {
                renderResults(emptyList())
            }
        }
    }

    private fun loadResults(position: Int) {
        val selectedItem = currentItems.getOrNull(position) ?: run {
            renderResults(emptyList())
            return
        }

        lifecycleScope.launch {
            val results = withContext(Dispatchers.IO) {
                repository.getRelatedTitles(currentMode, selectedItem.id)
            }
            renderResults(results)
        }
    }

    private fun renderResults(items: List<String>) {
        adapter.submitList(items)
        emptyTextView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    }
}
