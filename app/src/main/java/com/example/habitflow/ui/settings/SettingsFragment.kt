package com.example.habitflow.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.habitflow.R
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.databinding.FragmentSettingsBinding
import com.example.habitflow.ui.progress.PlayerProgress
import com.example.habitflow.util.LocaleManager
import com.example.habitflow.util.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // Edit Profile dialog fields
    private var dialogImageView: ImageView? = null
    private var dialogNameEditText: EditText? = null
    private var pendingPhotoUri: Uri? = null

    // ===== IMAGE PICKER =====
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        pendingPhotoUri = uri
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {}
        }
        dialogImageView?.setImageURI(uri)
    }

    // ===== LOCATION PERMISSION =====
    private val requestLocationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fineGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) toast("Location turned on")
        else toast("Location permission denied")
    }

    private fun askForLocationPermission() {
        val fine = Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = Manifest.permission.ACCESS_COARSE_LOCATION

        val already = ContextCompat.checkSelfPermission(requireContext(), fine) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), coarse) ==
                PackageManager.PERMISSION_GRANTED

        if (already) return toast("Location already enabled")

        val rationale = shouldShowRequestPermissionRationale(fine) ||
                shouldShowRequestPermissionRationale(coarse)

        if (rationale) {
            AlertDialog.Builder(requireContext())
                .setTitle("Location access")
                .setMessage("Give HabitFlow access to location for nearby features.")
                .setPositiveButton("Allow") { _, _ ->
                    requestLocationPermissionsLauncher.launch(arrayOf(fine, coarse))
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            requestLocationPermissionsLauncher.launch(arrayOf(fine, coarse))
        }
    }

    // ===== ANDROID 13+ NOTIFICATION PERMISSION =====
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) toast("Notifications enabled")
        else toast("Notifications denied")
    }

    private fun askForNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            return toast("Your Android version does not need notification permission")

        val permission = Manifest.permission.POST_NOTIFICATIONS
        val granted = ContextCompat.checkSelfPermission(
            requireContext(), permission
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) return toast("Notifications are already enabled")

        requestNotificationPermissionLauncher.launch(permission)
    }

    // ===== VIEW =====
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // ===== LOGIC =====
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUserHeader()
        setupClicks()
        setupLanguageSelector()
        debugPrintFcmToken()
    }

    private fun bindUserHeader() {
        binding.txtLevel.text = "Level: ${PlayerProgress.get(requireContext()).level}"

        lifecycleScope.launch(Dispatchers.IO) {
            val email = SessionManager(requireContext()).getUserSession()
            val db = AppDatabase.getInstance(requireContext())

            val user = email?.let { db.userDao().getUserByEmail(it) }

            withContext(Dispatchers.Main) {
                binding.txtUsername.text = user?.name ?: "Username"
                if (!user?.photoUri.isNullOrBlank()) {
                    binding.imgProfile.setImageURI(Uri.parse(user.photoUri))
                }
            }
        }
    }

    private fun setupClicks() = with(binding) {
        btnEditProfile.setOnClickListener { showEditProfileDialog() }

        // Navigation features
        btnAchievements.setOnClickListener { findNavController().navigate(R.id.achievementsFragment) }
        btnUnlockedSkills.setOnClickListener { findNavController().navigate(R.id.unlockedSkillsFragment) }
        btnStats.setOnClickListener { findNavController().navigate(R.id.statsFragment) }

        btnEditPersonalInfo.setOnClickListener { findNavController().navigate(R.id.personalInfoFragment) }
        btnChangePassword.setOnClickListener { findNavController().navigate(R.id.changePasswordFragment) }
        btnBanking.setOnClickListener { findNavController().navigate(R.id.bankingFragment) }

        btnLocation.setOnClickListener { askForLocationPermission() }
        btnNotifications.setOnClickListener { askForNotificationsPermission() }
        btnPrivacyPolicy.setOnClickListener { findNavController().navigate(R.id.privacyPolicyFragment) }
    }

    // ===== LANGUAGE SWITCH =====
    private fun setupLanguageSelector() {
        val btnLanguage: Button = binding.root.findViewById(R.id.btnLanguage)
        btnLanguage.setOnClickListener { showLanguageDialog() }
    }

    private fun showLanguageDialog() {
        val ctx = requireContext()
        val names = arrayOf(
            getString(R.string.lang_english),
            getString(R.string.lang_afrikaans),
            getString(R.string.lang_zulu)
        )
        val codes = arrayOf(
            LocaleManager.LANG_ENGLISH,
            LocaleManager.LANG_AFRIKAANS,
            LocaleManager.LANG_ZULU
        )

        val current = LocaleManager.getSavedLanguage(ctx)
        val currentIndex = codes.indexOf(current).coerceAtLeast(0)

        AlertDialog.Builder(ctx)
            .setTitle(R.string.choose_language)
            .setSingleChoiceItems(names, currentIndex) { dialog, which ->
                LocaleManager.saveLanguage(ctx, codes[which])
                requireActivity().recreate()
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    // ===== FCM Debug =====
    private fun debugPrintFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.d("FCM", "Token: $it")
        }
    }

    // ===== EDIT PROFILE DIALOG =====
    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        dialogImageView = dialogView.findViewById(R.id.imgProfilePreview)
        dialogNameEditText = dialogView.findViewById(R.id.etDisplayName)

        pendingPhotoUri = null

        lifecycleScope.launch(Dispatchers.IO) {
            val email = SessionManager(requireContext()).getUserSession()
            val db = AppDatabase.getInstance(requireContext())
            val user = email?.let { db.userDao().getUserByEmail(it) }

            withContext(Dispatchers.Main) {
                dialogNameEditText?.setText(user?.name ?: "")
                if (!user?.photoUri.isNullOrEmpty()) {
                    dialogImageView?.setImageURI(Uri.parse(user.photoUri))
                }
            }
        }

        val alert = AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialogView.findViewById<Button>(R.id.btnPickPhoto).setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        alert.setOnShowListener {
            val saveBtn = alert.getButton(AlertDialog.BUTTON_POSITIVE)
            saveBtn.setOnClickListener {
                val newName = dialogNameEditText?.text?.toString()?.trim().orEmpty()
                val previewUri = pendingPhotoUri

                lifecycleScope.launch(Dispatchers.IO) {
                    val session = SessionManager(requireContext()).getUserSession()
                    if (session != null) {
                        val db = AppDatabase.getInstance(requireContext())
                        if (newName.isNotBlank()) db.userDao().updateUserName(session, newName)
                        db.userDao().updateUserPhoto(session, previewUri?.toString())
                    }
                    withContext(Dispatchers.Main) {
                        binding.txtUsername.text = newName
                        if (previewUri != null) binding.imgProfile.setImageURI(previewUri)
                        toast("Profile updated")
                        alert.dismiss()
                    }
                }
            }
        }

        alert.show()
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
