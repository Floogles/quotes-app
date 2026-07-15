package com.floogles.dailyquote

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.floogles.dailyquote.databinding.ItemQuoteBinding

class QuoteAdapter(
    private var quotes: List<Quote>,
    private val onClick: (Quote) -> Unit,
    private val onDelete: (Quote) -> Unit
) : RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder>() {

    inner class QuoteViewHolder(val binding: ItemQuoteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val binding = ItemQuoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return QuoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val quote = quotes[position]
        holder.binding.quoteText.text = quote.text
        if (quote.author.isNotBlank()) {
            holder.binding.quoteAuthor.text = "— ${quote.author}"
            holder.binding.quoteAuthor.visibility = ViewGroup.VISIBLE
        } else {
            holder.binding.quoteAuthor.visibility = ViewGroup.GONE
        }
        holder.binding.root.setOnClickListener { onClick(quote) }
        holder.binding.deleteButton.setOnClickListener { onDelete(quote) }
    }

    override fun getItemCount(): Int = quotes.size

    fun submit(newQuotes: List<Quote>) {
        quotes = newQuotes
        notifyDataSetChanged()
    }
}
