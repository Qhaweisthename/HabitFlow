package com.example.habitflow

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavOptions
import androidx.navigation.ui.setupWithNavController
import com.example.habitflow.data.model.Task
import com.example.habitflow.ui.LoginActivity
import com.example.habitflow.ui.tasks.TaskViewModel
import com.example.habitflow.util.SessionManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize session and check login
        sessionManager = SessionManager(this)
        val userSession = sessionManager.getUserSession()

        if (userSession.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // ✅ Load main layout
        setContentView(R.layout.activity_main)

        // ✅ Setup top bar and bottom nav
        val topBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // ✅ Setup Navigation
        val host = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = host.navController
        bottomNav.setupWithNavController(navController)

        // Force navigation to the selected top-level destination from anywhere
        bottomNav.setOnItemSelectedListener { item ->
            val options = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(navController.graph.startDestinationId, false, saveState = true)
                .build()
            return@setOnItemSelectedListener try {
                navController.navigate(item.itemId, null, options)
                true
            } catch (_: IllegalArgumentException) {
                false
            }
        }

        // If reselected, pop back to that destination (clears nested stack under it)
        bottomNav.setOnItemReselectedListener { menuItem ->
            navController.popBackStack(menuItem.itemId, false)
        }

        // ✅ Display Welcome Text
        //topBar.title = "Welcome, ${userSession ?: "User"}"

        // ✅ Inflate Logout Menu
        topBar.inflateMenu(R.menu.menu_top_appbar)

        // ✅ Handle Logout Menu Click
        topBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    showLogoutConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    // ✅ Task Dialog (still available if needed)
    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val etTaskName = dialogView.findViewById<EditText>(R.id.etTaskName)
        val tvPickedDate = dialogView.findViewById<TextView>(R.id.tvPickedDate)

        var pickedDate = Task.getTodayDate()
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

    // ✅ Logout Confirmation Dialog
    fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                sessionManager.clearSession()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}
