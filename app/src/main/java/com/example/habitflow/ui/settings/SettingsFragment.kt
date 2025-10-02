package com.example.habitflow.ui.settings

import android.annotation.SuppressLint
import com.example.habitflow.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.habitflow.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // Replace with your real data sources
    data class User(val username: String, val level: Int, val photoRes: Int? = null)

    private fun getCurrentUser(): User {
        // TODO: Integrate with your auth/profile/DB layer
        return User(username = "Username", level = getCurrentLevel(), photoRes = null)
    }

    private fun getCurrentLevel(): Int {
        // TODO: Use your Level class/repository
        return 2
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUserHeader()
        setupClicks()
    }

    @SuppressLint("StringFormatInvalid")
    private fun bindUserHeader() {
        val user = getCurrentUser()
        binding.txtUsername.text = user.username
        binding.txtLevel.text = getString(R.string.level_format, user.level)

        if (user.photoRes != null) {
            binding.imgProfile.setImageResource(user.photoRes)
        } else {
            binding.imgProfile.setImageResource(android.R.drawable.sym_def_app_icon)
        }
    }

    private fun setupClicks() = with(binding) {
        // Header gear -> edit profile
        btnEditProfile.setOnClickListener { toast("Edit Profile") }

        // Profile
        btnCustomizeAvatar.setOnClickListener { toast("Customize Avatar") }
        btnEquipment.setOnClickListener { toast("Equipment") }
        btnItems.setOnClickListener { toast("Items") }
        btnPetsMounts.setOnClickListener { toast("Pets & Mounts") }
        btnUpgradePremium.setOnClickListener { toast("Upgrade (coming soon)") }

        // Skills
        btnAchievements.setOnClickListener { toast("Achievements") }
        btnUnlockedSkills.setOnClickListener { toast("Unlocked Skills") }
        btnStats.setOnClickListener { toast("Stats & Upgrades") }

        // Privacy
        btnEditPersonalInfo.setOnClickListener { toast("Personal Information") }
        btnChangePassword.setOnClickListener { toast("Password Management") }
        btnBanking.setOnClickListener { toast("Banking Information") }
        btnLocation.setOnClickListener { toast("Location") }
        btnNotifications.setOnClickListener { toast("Notifications") }
        btnPrivacyPolicy.setOnClickListener { toast("Privacy Policy") }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}