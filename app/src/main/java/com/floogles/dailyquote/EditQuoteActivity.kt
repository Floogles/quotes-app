package com.floogles.dailyquote

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.floogles.dailyquote.databinding.ActivityEditQuoteBinding

class EditQuoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditQuoteBinding
    private var editingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditQuoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingId = intent.getStringExtra(EXTRA_QUOTE_ID)

        if (editingId != null) {
            title = getString(R.string.edit_quote)
            val quote = QuoteStore.get(this, editingId!!)
            if (quote != null) {
                binding.quoteInput.setText(quote.text)
                binding.authorInput.setText(quote.author)
            }
            binding.deleteButton.visibility = View.VISIBLE
        } else {
            title = getString(R.string.add_quote)
            binding.deleteButton.visibility = View.GONE
        }

        binding.saveButton.setOnClickListener { save() }
        binding.cancelButton.setOnClickListener { finish() }
        binding.deleteButton.setOnClickListener {
            editingId?.let { QuoteStore.delete(this, it) }
            DailyQuoteWidget.updateAll(this)
            finish()
        }
    }

    private fun save() {
        val text = binding.quoteInput.text?.toString()?.trim().orEmpty()
        val author = binding.authorInput.text?.toString()?.trim().orEmpty()

        if (text.isEmpty()) {
            binding.quoteInputLayout.error = getString(R.string.error_empty_quote)
            return
        }
        binding.quoteInputLayout.error = null

        val id = editingId
        if (id == null) {
            QuoteStore.add(this, text, author)
        } else {
            QuoteStore.update(this, id, text, author)
        }
        DailyQuoteWidget.updateAll(this)
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        const val EXTRA_QUOTE_ID = "extra_quote_id"
    }
}
