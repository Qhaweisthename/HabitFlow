package com.example.habitflow.ui.settings.banking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habitflow.databinding.DialogEditCardBinding
import com.example.habitflow.databinding.FragmentBankingBinding

data class BankingCard(
    var id: Int,
    var holderName: String,
    var number: String, // store as plain for demo; in production, never store raw PANs
    var expiry: String
)

class BankingFragment : Fragment(), BankingCardsAdapter.CardCallbacks {
    private var _binding: FragmentBankingBinding? = null
    private val binding get() = _binding!!

    private val cards = mutableListOf<BankingCard>()
    private lateinit var adapter: BankingCardsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBankingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Dummy initial card
        if (cards.isEmpty()) {
            cards.add(
                BankingCard(
                    id = 1,
                    holderName = "John Doe",
                    number = "4242424242424242",
                    expiry = "12/26"
                )
            )
        }

        adapter = BankingCardsAdapter(cards, this)
        binding.rvCards.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCards.adapter = adapter

        binding.btnAddCard.setOnClickListener { showEditCardDialog(null) }
    }

    private fun showEditCardDialog(existing: BankingCard?) {
        val dialogBinding = DialogEditCardBinding.inflate(layoutInflater)
        if (existing != null) {
            dialogBinding.etHolderName.setText(existing.holderName)
            dialogBinding.etCardNumber.setText(existing.number)
            dialogBinding.etExpiry.setText(existing.expiry)
        }

        val title = if (existing == null) "Add Card" else "Edit Card"
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(dialogBinding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val name = dialogBinding.etHolderName.text?.toString()?.trim().orEmpty()
                val number = dialogBinding.etCardNumber.text?.toString()?.replace(" ", "")?.trim().orEmpty()
                val expiry = dialogBinding.etExpiry.text?.toString()?.trim().orEmpty()

                if (name.isEmpty() || number.length < 12 || !expiry.matches(Regex("^(0[1-9]|1[0-2])\\/\\d{2}$"))) {
                    dialogBinding.tilHolderName.error = if (name.isEmpty()) "Required" else null
                    dialogBinding.tilCardNumber.error = if (number.length < 12) "Invalid card" else null
                    dialogBinding.tilExpiry.error = if (!expiry.matches(Regex("^(0[1-9]|1[0-2])\\/\\d{2}$"))) "MM/YY" else null
                    return@setOnClickListener
                }

                if (existing == null) {
                    val newId = (cards.maxOfOrNull { it.id } ?: 0) + 1
                    cards.add(BankingCard(newId, name, number, expiry))
                } else {
                    existing.holderName = name
                    existing.number = number
                    existing.expiry = expiry
                }
                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    override fun onEdit(card: BankingCard) {
        showEditCardDialog(card)
    }

    override fun onDelete(card: BankingCard) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete card?")
            .setMessage("This will remove the saved card from HabitFlow.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                cards.remove(card)
                adapter.notifyDataSetChanged()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
