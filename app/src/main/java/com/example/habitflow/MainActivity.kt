package com.example.habitflow

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.habitflow.model.Task
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🔹 Global FAB
        fab = findViewById(R.id.fabGlobalAddTask)
        fab.setOnClickListener {
            showAddTaskDialog()
        }

        // 🔹 Setup Navigation
        val host = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = host.navController

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottom.setupWithNavController(navController)

        val topBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        // 🔹 Show/Hide FAB depending on current fragment
        navController.addOnDestinationChangedListener { _, dest, _ ->
            topBar.title = dest.label
            fab.hide() // Hide by default

            if (dest.id == R.id.tasksFragment) {
                fab.show()
            }
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val etTaskName = dialogView.findViewById<EditText>(R.id.etTaskName)
        val tvPickedDate = dialogView.findViewById<TextView>(R.id.tvPickedDate)

        var pickedDate = Task.getTodayDate() // default today
        tvPickedDate.text = pickedDate

        tvPickedDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    calendar.set(year, month, dayOfMonth)
                    pickedDate = format.format(calendar.time)
                    tvPickedDate.text = pickedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        AlertDialog.Builder(this)
            .setTitle("New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etTaskName.text.toString()
                if (name.isNotBlank()) {
                    val viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
                    viewModel.addTask(viewModel.newTask(name, pickedDate))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
