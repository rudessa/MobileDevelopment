package com.example.airtickets


import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerDeparture: Spinner
    private lateinit var spinnerArrival: Spinner
    private lateinit var editDepartureDate: EditText
    private lateinit var editReturnDate: EditText
    private lateinit var editAdults: EditText
    private lateinit var editChildren: EditText
    private lateinit var editInfants: EditText
    private lateinit var btnSearch: Button

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupSpinners()
        setupDatePickers()
        setupSearchButton()
    }

    private fun initViews() {
        spinnerDeparture = findViewById(R.id.spinnerDeparture)
        spinnerArrival = findViewById(R.id.spinnerArrival)
        editDepartureDate = findViewById(R.id.editDepartureDate)
        editReturnDate = findViewById(R.id.editReturnDate)
        editAdults = findViewById(R.id.editAdults)
        editChildren = findViewById(R.id.editChildren)
        editInfants = findViewById(R.id.editInfants)
        btnSearch = findViewById(R.id.btnSearch)
    }

    private fun setupSpinners() {
        val cities = resources.getStringArray(R.array.cities)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerDeparture.adapter = adapter
        spinnerArrival.adapter = adapter
    }

    private fun setupDatePickers() {
        editDepartureDate.isFocusable = false
        editDepartureDate.isClickable = true
        editDepartureDate.setOnClickListener {
            showDatePicker { date ->
                editDepartureDate.setText(date)
            }
        }

        editReturnDate.isFocusable = false
        editReturnDate.isClickable = true
        editReturnDate.setOnClickListener {
            showDatePicker { date ->
                editReturnDate.setText(date)
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            onDateSelected(dateFormat.format(calendar.time))
        }, year, month, day).show()
    }

    private fun setupSearchButton() {
        btnSearch.setOnClickListener {
            val departureCity = spinnerDeparture.selectedItem.toString()
            val arrivalCity = spinnerArrival.selectedItem.toString()
            val departureDate = editDepartureDate.text.toString()
            val returnDate = editReturnDate.text.toString()
            val adults = editAdults.text.toString()
            val children = editChildren.text.toString()
            val infants = editInfants.text.toString()

            if (validateInputs(departureCity, arrivalCity, departureDate, returnDate)) {
                val message = """
                    Поиск билетов:
                    Откуда: $departureCity
                    Куда: $arrivalCity
                    Дата вылета: $departureDate
                    Дата возврата: $returnDate
                    Взрослые: $adults
                    Дети: $children
                    Младенцы: $infants
                """.trimIndent()

                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validateInputs(
        departure: String,
        arrival: String,
        departureDate: String,
        returnDate: String
    ): Boolean {
        if (departure == "Выберите город") {
            Toast.makeText(this, "Выберите город вылета", Toast.LENGTH_SHORT).show()
            return false
        }
        if (arrival == "Выберите город") {
            Toast.makeText(this, "Выберите город прилёта", Toast.LENGTH_SHORT).show()
            return false
        }
        if (departureDate.isEmpty()) {
            Toast.makeText(this, "Выберите дату вылета", Toast.LENGTH_SHORT).show()
            return false
        }
        if (returnDate.isEmpty()) {
            Toast.makeText(this, "Выберите дату возврата", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}