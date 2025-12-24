package com.helow.runner4

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.auth
import com.helow.runner4.auth.AuthActivity
import com.helow.runner4.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy(LazyThreadSafetyMode.NONE) { ActivityMainBinding.inflate(layoutInflater) }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                step2EnsureLocationPermission()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                step1EnsureNotificationsPermission()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.notifications_denied))
                    .setMessage(getString(R.string.permission_required, getString(R.string.send_notifications)))
                    .setPositiveButton(getString(R.string.to_settings)) { _, _ -> openAppSettings() }
                    .setNegativeButton(getString(R.string.logout)) { _, _ -> finish() }
                    .show()
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                recreate()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                step2EnsureLocationPermission()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.location_denied))
                    .setMessage(getString(R.string.permission_required, getString(R.string.fine_location_access)))
                    .setPositiveButton(getString(R.string.to_settings)) { _, _ -> openAppSettings() }
                    .setNegativeButton(getString(R.string.logout)) { _, _ -> finish() }
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        Firebase.analytics.setUserId(Firebase.auth.uid)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.toolbar.updatePadding(top = systemBars.top)
            binding.bottomNavigationView.updatePadding(bottom = systemBars.bottom)
            v.updatePadding(left = systemBars.left, right = systemBars.right)

            insets
        }

        NotificationManagerCompat.from(this).createNotificationChannel(
            NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.current_run), NotificationManager.IMPORTANCE_DEFAULT)
        )

        setSupportActionBar(binding.toolbar)

        val navHost = supportFragmentManager.findFragmentById(R.id.navFragment) as NavHostFragment
        val navController = navHost.navController

        setupActionBarWithNavController(navController, AppBarConfiguration(binding.bottomNavigationView.menu))
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)

        step1EnsureNotificationsPermission()
    }

    private fun step1EnsureNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            if (granted != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        step2EnsureLocationPermission()
    }

    private fun step2EnsureLocationPermission() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        if (granted != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.log_out) {
            Firebase.auth.signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp() = findNavController(R.id.navFragment).navigateUp() || super.onSupportNavigateUp()
}