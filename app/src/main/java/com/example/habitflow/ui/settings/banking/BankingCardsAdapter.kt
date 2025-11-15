package com.example.habitflow.ui.settings.banking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habitflow.databinding.ItemBankingCardBinding

class BankingCardsAdapter(
    private val items: List<BankingCard>,
    private val callbacks: CardCallbacks
) : RecyclerView.Adapter<BankingCardsAdapter.CardVH>() {

    interface CardCallbacks {
        fun onEdit(card: BankingCard)
        fun onDelete(card: BankingCard)
    }

    inner class CardVH(val binding: ItemBankingCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBankingCardBinding.inflate(inflater, parent, false)
        return CardVH(binding)
    }

    override fun onBindViewHolder(holder: CardVH, position: Int) {
        val card = items[position]
        val masked = card.number.takeLast(4).padStart(card.number.length, '•')
        holder.binding.tvHolder.text = card.holderName
        holder.binding.tvNumber.text = masked
        holder.binding.tvExpiry.text = card.expiry

        holder.binding.btnEdit.setOnClickListener { callbacks.onEdit(card) }
        holder.binding.btnDelete.setOnClickListener { callbacks.onDelete(card) }
    }

    override fun getItemCount(): Int = items.size
}