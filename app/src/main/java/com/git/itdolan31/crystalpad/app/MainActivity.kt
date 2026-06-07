/*
 * Crystalpad
 * Copyright (C) 2026 itdolan31
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.git.itdolan31.crystalpad.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import com.git.itdolan31.crystalpad.features.app_lock.AppLockScreen
import com.git.itdolan31.crystalpad.navigation.CrystalPadNavHost
import com.git.itdolan31.crystalpad.navigation.Screen
import com.git.itdolan31.crystalpad.ui.theme.CrystalPadTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var lockJob: Job? = null
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !(viewModel.themeLoaded && viewModel.passwordLoaded)
        }

        enableEdgeToEdge()

        setContent {
            val backStack = rememberNavBackStack(Screen.Home)

            val theme by viewModel.theme.collectAsStateWithLifecycle()
            val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()

            CrystalPadTheme(themeType = theme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isLocked) {
                        AppLockScreen(
                            onUnlocked = { viewModel.setLocked(false) })
                    } else {
                        CrystalPadNavHost(backStack)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isFlagSecureEnabled.collect { enabled ->
                    if (enabled) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lockJob?.cancel()
        lockJob = null
    }

    override fun onStop() {
        super.onStop()

        val timeout = viewModel.lockTimeout
        val isPasswordSet = viewModel.isPasswordSet

        if (isPasswordSet) {
            if (timeout > 0) {
                lockJob = lifecycleScope.launch {
                    delay(timeout * 1000L)
                    viewModel.setLocked(true)
                }
            } else if (timeout == 0) {
                viewModel.setLocked(true)
            }
        }
    }
}