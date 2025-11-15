package com.example.habitflow.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.databinding.FragmentPersonalInfoBinding
import com.example.habitflow.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PersonalInfoFragment : Fragment() {
    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val email = SessionManager(requireContext()).getUserSession()
            val db = Room.databaseBuilder(
                requireContext().applicationContext,
                AppDatabase::class.java,
                "habitflow_db"
            ).fallbackToDestructiveMigration().build()

            val user = email?.let { db.userDao().getUserByEmail(it) }
            withContext(Dispatchers.Main) {
                binding.tvFullName.text = user?.name ?: ""
                binding.tvEmail.text = email ?: ""
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
