package com.example.habitflow.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.databinding.FragmentChangePasswordBinding
import com.example.habitflow.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangePasswordFragment : Fragment() {
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSavePassword.setOnClickListener {
            val current = binding.etCurrent.text?.toString()?.trim().orEmpty()
            val newPwd = binding.etNew.text?.toString()?.trim().orEmpty()
            val confirm = binding.etConfirm.text?.toString()?.trim().orEmpty()

            // Reset errors
            binding.tilCurrent.error = null
            binding.tilNew.error = null
            binding.tilConfirm.error = null

            var hasError = false
            if (current.isEmpty()) {
                binding.tilCurrent.error = "Required"
                hasError = true
            }
            if (newPwd.length < 6) {
                binding.tilNew.error = "At least 6 characters"
                hasError = true
            }
            if (confirm != newPwd) {
                binding.tilConfirm.error = "Passwords do not match"
                hasError = true
            }
            if (hasError) return@setOnClickListener

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val sessionEmail = SessionManager(requireContext()).getUserSession()
                if (sessionEmail.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        toast("No session. Please log in again.")
                    }
                    return@launch
                }

                val db = Room.databaseBuilder(
                    requireContext().applicationContext,
                    AppDatabase::class.java,
                    "habitflow_db"
                ).fallbackToDestructiveMigration().build()

                // Verify current password
                val user = db.userDao().login(sessionEmail, current)
                if (user == null) {
                    withContext(Dispatchers.Main) {
                        binding.tilCurrent.error = "Incorrect password"
                    }
                    return@launch
                }

                val updated = db.userDao().updateUserPassword(sessionEmail, newPwd)
                withContext(Dispatchers.Main) {
                    if (updated > 0) {
                        toast("Password updated")
                        binding.etCurrent.setText("")
                        binding.etNew.setText("")
                        binding.etConfirm.setText("")
                    } else {
                        toast("Update failed. Try again.")
                    }
                }
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
