package com.example.habitflow.ui.settings

import com.example.habitflow.R
import android.net.Uri
import android.content.Intent
import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.room.Room
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.databinding.FragmentSettingsBinding
import com.example.habitflow.ui.progress.PlayerProgress
import com.example.habitflow.util.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // Edit Profile dialog views to update from photo picker callback
    private var dialogImageView: ImageView? = null
    private var dialogNameEditText: EditText? = null
    private var pendingPhotoUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        pendingPhotoUri = uri
        if (uri != null) {
            // Persist read permission across sessions
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { }
        }
        dialogImageView?.let { imageView ->
            imageView.setImageURI(uri)
        }
    }

    // Location permission launcher (foreground location)
    private val requestLocationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fineGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            toast("Location sharing enabled")
        } else {
            toast("Location permission denied")
        }
    }

    private fun askForLocationPermission() {
        val fine = Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = Manifest.permission.ACCESS_COARSE_LOCATION

        val hasFine = ContextCompat.checkSelfPermission(requireContext(), fine) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(requireContext(), coarse) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            toast("Location already enabled")
            return
        }

        val needsRationale = shouldShowRequestPermissionRationale(fine) || shouldShowRequestPermissionRationale(coarse)
        if (needsRationale) {
            AlertDialog.Builder(requireContext())
                .setTitle("Allow location sharing?")
                .setMessage("HabitFlow can use your location for location-based features. Do you want to allow location sharing?")
                .setNegativeButton("No", null)
                .setPositiveButton("Allow") { _, _ ->
                    requestLocationPermissionsLauncher.launch(arrayOf(fine, coarse))
                }
                .show()
        } else {
            requestLocationPermissionsLauncher.launch(arrayOf(fine, coarse))
        }
    }

    // Android 13+ notifications permission launcher
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            toast("Notifications enabled")
        } else {
            toast("Notifications permission denied")
        }
    }

    private fun askForNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // No runtime permission needed pre-Android 13
            AlertDialog.Builder(requireContext())
                .setTitle("Notifications")
                .setMessage("Notifications are enabled by default on your version of Android. You can manage them in system settings.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        val hasPermission = ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            toast("Notifications already enabled")
            return
        }

        val showRationale = shouldShowRequestPermissionRationale(permission)
        if (showRationale) {
            AlertDialog.Builder(requireContext())
                .setTitle("Allow notifications?")
                .setMessage("HabitFlow uses notifications to remind you about tasks and progress. Do you want to allow notifications?")
                .setNegativeButton("No", null)
                .setPositiveButton("Allow") { _, _ ->
                    requestNotificationPermissionLauncher.launch(permission)
                }
                .show()
        } else {
            // First time ask
            requestNotificationPermissionLauncher.launch(permission)
        }
    }

    private fun getCurrentLevel(): Int {
        return PlayerProgress.get(requireContext()).level
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        dialogImageView = dialogView.findViewById(R.id.imgProfilePreview)
        dialogNameEditText = dialogView.findViewById(R.id.etDisplayName)
        pendingPhotoUri = null

        // Prefill current values
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val session = SessionManager(requireContext()).getUserSession()
            if (session != null) {
                val db = Room.databaseBuilder(
                    requireContext().applicationContext,
                    AppDatabase::class.java,
                    "habitflow_db"
                ).fallbackToDestructiveMigration().build()
                val user = db.userDao().getUserByEmail(session)
                withContext(Dispatchers.Main) {
                    dialogNameEditText?.setText(user?.name ?: "")
                    if (!user?.photoUri.isNullOrEmpty()) {
                        try {
                            dialogImageView?.setImageURI(Uri.parse(user!!.photoUri))
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        val alert = AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save", null) // set later to prevent auto-dismiss
            .setNegativeButton("Cancel", null)
            .create()

        alert.setOnShowListener {
            dialogView.findViewById<Button>(R.id.btnPickPhoto).setOnClickListener {
                pickImageLauncher.launch(arrayOf("image/*"))
            }

            val saveBtn = alert.getButton(AlertDialog.BUTTON_POSITIVE)
            saveBtn.setOnClickListener {
                val newName = dialogNameEditText?.text?.toString()?.trim().orEmpty()
                val newPhoto = pendingPhotoUri
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val session = SessionManager(requireContext()).getUserSession()
                    var nameUpdated = 0
                    var photoUpdated = 0
                    if (session != null) {
                        val db = Room.databaseBuilder(
                            requireContext().applicationContext,
                            AppDatabase::class.java,
                            "habitflow_db"
                        ).fallbackToDestructiveMigration().build()
                        if (newName.isNotEmpty()) nameUpdated = db.userDao().updateUserName(session, newName)
                        photoUpdated = db.userDao().updateUserPhoto(session, newPhoto?.toString())
                    }
                    withContext(Dispatchers.Main) {
                        // Update header immediately for better UX
                        if (newName.isNotEmpty()) {
                            binding.txtUsername.text = newName
                        }
                        if (newPhoto != null) {
                            try {
                                binding.imgProfile.setImageURI(newPhoto)
                            } catch (_: Exception) {
                                binding.imgProfile.setImageResource(android.R.drawable.sym_def_app_icon)
                            }
                        }

                        if (nameUpdated == 0 && photoUpdated == 0) {
                            toast("No user found. Please log in again.")
                        } else {
                            toast("Profile updated")
                        }

                        alert.dismiss()
                    }
                }
            }
        }

        alert.show()
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

    private fun bindUserHeader() {
        // Level label
        binding.txtLevel.text = "Level: ${getCurrentLevel()}"

        // Load registered user's name from Room using session email
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val session = SessionManager(requireContext()).getUserSession()
            if (session == null) {
                withContext(Dispatchers.Main) {
                    binding.txtUsername.text = "Username"
                    binding.imgProfile.setImageResource(android.R.drawable.sym_def_app_icon)
                }
                return@launch
            }

            val db = Room.databaseBuilder(
                requireContext().applicationContext,
                AppDatabase::class.java,
                "habitflow_db"
            ).fallbackToDestructiveMigration().build()

            val user = db.userDao().getUserByEmail(session)
            withContext(Dispatchers.Main) {
                binding.txtUsername.text = user?.name ?: "Username"
                if (!user?.photoUri.isNullOrEmpty()) {
                    try {
                        binding.imgProfile.setImageURI(Uri.parse(user!!.photoUri))
                    } catch (_: Exception) {
                        binding.imgProfile.setImageResource(android.R.drawable.sym_def_app_icon)
                    }
                } else {
                    binding.imgProfile.setImageResource(android.R.drawable.sym_def_app_icon)
                }
            }
        }
    }

    private fun setupClicks() = with(binding) {
        // Header gear -> edit profile
        btnEditProfile.setOnClickListener { showEditProfileDialog() }

        // Profile
//        btnCustomizeAvatar.setOnClickListener { findNavController().navigate(R.id.customizeAvatarFragment) }
//        btnEquipment.setOnClickListener { findNavController().navigate(R.id.equipmentFragment) }
//        btnItems.setOnClickListener { findNavController().navigate(R.id.itemsFragment) }
//        btnPetsMounts.setOnClickListener { findNavController().navigate(R.id.petsMountsFragment) }
//        btnUpgradePremium.setOnClickListener { findNavController().navigate(R.id.upgradePremiumFragment) }

        // Skills
        btnAchievements.setOnClickListener { findNavController().navigate(R.id.achievementsFragment) }
        btnUnlockedSkills.setOnClickListener { findNavController().navigate(R.id.unlockedSkillsFragment) }
        btnStats.setOnClickListener { findNavController().navigate(R.id.statsFragment) }

        // Privacy
        btnEditPersonalInfo.setOnClickListener { findNavController().navigate(R.id.personalInfoFragment) }
        btnChangePassword.setOnClickListener { findNavController().navigate(R.id.changePasswordFragment) }
        btnBanking.setOnClickListener { findNavController().navigate(R.id.bankingFragment) }
        btnLocation.setOnClickListener { askForLocationPermission() }
        btnNotifications.setOnClickListener { askForNotificationsPermission() }
        btnPrivacyPolicy.setOnClickListener { findNavController().navigate(R.id.privacyPolicyFragment) }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}