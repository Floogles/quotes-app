package com.floogles.dailyquote

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.floogles.dailyquote.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: QuoteAdapter

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    private val exportQuotes =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri?.let { writeBackup(it) }
        }

    private val importQuotes =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { readBackup(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        Notifications.ensureChannel(this)
        AlarmScheduler.schedule(this)
        maybeRequestNotificationPermission()

        adapter = QuoteAdapter(
            quotes = emptyList(),
            onClick = { quote -> openEditor(quote.id) },
            onDelete = { quote -> confirmDelete(quote) }
        )
        binding.quotesList.layoutManager = LinearLayoutManager(this)
        binding.quotesList.adapter = adapter

        binding.addButton.setOnClickListener { openEditor(null) }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val quotes = QuoteStore.getAll(this)
        adapter.submit(quotes)

        // Empty state
        binding.emptyView.visibility = if (quotes.isEmpty()) View.VISIBLE else View.GONE

        // "Quote of the day" card
        val today = QuoteStore.quoteOfDay(this)
        if (today == null) {
            binding.todayQuote.text = getString(R.string.today_empty)
            binding.todayAuthor.visibility = View.GONE
        } else {
            binding.todayQuote.text = today.text
            if (today.author.isNotBlank()) {
                binding.todayAuthor.text = "— ${today.author}"
                binding.todayAuthor.visibility = View.VISIBLE
            } else {
                binding.todayAuthor.visibility = View.GONE
            }
        }

        DailyQuoteWidget.updateAll(this)
    }

    private fun openEditor(quoteId: String?) {
        val intent = Intent(this, EditQuoteActivity::class.java)
        if (quoteId != null) {
            intent.putExtra(EditQuoteActivity.EXTRA_QUOTE_ID, quoteId)
        }
        startActivity(intent)
    }

    private fun confirmDelete(quote: Quote) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                QuoteStore.delete(this, quote.id)
                refresh()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                exportQuotes.launch(getString(R.string.export_filename))
                true
            }
            R.id.action_import -> {
                importQuotes.launch(arrayOf("application/json", "text/plain", "*/*"))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun writeBackup(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { out ->
                out.write(QuoteStore.exportToJson(this).toByteArray())
            }
            Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.export_failed, Toast.LENGTH_LONG).show()
        }
    }

    private fun readBackup(uri: Uri) {
        try {
            val json = contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            } ?: return
            val added = QuoteStore.importFromJson(this, json)
            refresh()
            val message = if (added > 0) {
                resources.getQuantityString(R.plurals.import_success, added, added)
            } else {
                getString(R.string.import_none)
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.import_failed, Toast.LENGTH_LONG).show()
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !Notifications.hasPermission(this)
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
