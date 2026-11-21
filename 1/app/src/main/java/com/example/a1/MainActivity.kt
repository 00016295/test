package com.example.a1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a1.data.CallReminderReciever
import com.example.a1.data.ContactAdapter
import com.example.a1.data.ContactsRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var accessDeniedView: View
    private lateinit var grantButton: Button
    private lateinit var adapter: ContactAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadContacts()
        } else {
            showAccessDeniedState()
        }
    }

    private val requestNotificationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Notification Off", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        accessDeniedView = findViewById(R.id.accessDeniedLayout)
        grantButton = findViewById(R.id.btnGrant)

        setupRecyclerView()

        grantButton.setOnClickListener {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            } else {
                val settingsIntent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                startActivity(settingsIntent)
            }
        }
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val hasContactsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasContactsPermission) {
            loadContacts()
        } else {
            showAccessDeniedState()
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasNotificationPermission) {
                requestNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun loadContacts(forceRefresh: Boolean = false) {
        showListState()
        lifecycleScope.launch {
            if (forceRefresh) {
                ContactsRepository.refresh(this@MainActivity)
            } else {
                ContactsRepository.load(this@MainActivity)
            }
            adapter.submitList(ContactsRepository.contacts.toList())
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ContactAdapter { contact ->
            Toast.makeText(this, "Reminder created", Toast.LENGTH_SHORT).show()
            val intent = CallReminderReciever.createIntent(this, contact.id)
            sendBroadcast(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun showListState() {
        recyclerView.visibility = View.VISIBLE
        accessDeniedView.visibility = View.GONE
    }

    private fun showAccessDeniedState() {
        recyclerView.visibility = View.GONE
        accessDeniedView.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_refresh) {
            loadContacts(forceRefresh = true)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() { super.onStart(); Log.d(TAG, "onStart") }
    override fun onResume() { super.onResume(); Log.d(TAG, "onResume") }
    override fun onPause() { super.onPause(); Log.d(TAG, "onPause") }
    override fun onStop() { super.onStop(); Log.d(TAG, "onStop") }
    override fun onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy") }
}