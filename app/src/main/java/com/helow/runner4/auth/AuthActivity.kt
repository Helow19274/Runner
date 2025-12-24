package com.helow.runner4.auth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.helow.runner4.R
import com.helow.runner4.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAuthBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.toolbar.updatePadding(top = systemBars.top)
            v.updatePadding(left = systemBars.left, right = systemBars.right, bottom = systemBars.bottom)

            insets
        }

        setSupportActionBar(binding.toolbar)

        val navHost = supportFragmentManager.findFragmentById(R.id.navFragment) as NavHostFragment
        val navController = navHost.navController

        setupActionBarWithNavController(
            navController,
            AppBarConfiguration(setOf(R.id.loginFragment, R.id.registerFragment))
        )
    }
}