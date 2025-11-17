package com.example.habitflow.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.habitflow.util.LocaleManager
import com.example.habitflow.R

class SettingsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No Firebase in this branch, so nothing extra here
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Your original purple settings layout
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Only language button is required for your PoE feature
        val btnLanguage: Button = view.findViewById(R.id.btnLanguage)

        btnLanguage.setOnClickListener {
            showLanguageDialog()
        }
    }

    /**
     * Shows the dialog for EN / AF / ZU.
     * Saves the choice and recreates the Activity so all screens update.
     */
    private fun showLanguageDialog() {
        val ctx = requireContext()

        // Names shown in dialog
        val names = arrayOf<CharSequence>(
            getString(R.string.lang_english),
            getString(R.string.lang_afrikaans),
            getString(R.string.lang_zulu)
        )

        // Codes we save
        val codes = arrayOf(
            LocaleManager.LANG_ENGLISH,
            LocaleManager.LANG_AFRIKAANS,
            LocaleManager.LANG_ZULU
        )

        val currentCode = LocaleManager.getSavedLanguage(ctx)
        var selectedIndex = codes.indexOf(currentCode)
        if (selectedIndex == -1) selectedIndex = 0

        AlertDialog.Builder(ctx)
            .setTitle(getString(R.string.choose_language))
            .setSingleChoiceItems(names, selectedIndex) { dialog, which ->
                val newCode = codes[which]
                if (newCode != currentCode) {
                    LocaleManager.saveLanguage(ctx, newCode)
                    requireActivity().recreate()
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}