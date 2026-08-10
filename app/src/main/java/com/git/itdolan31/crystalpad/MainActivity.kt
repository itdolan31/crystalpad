package com.git.itdolan31.crystalpad

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
import com.git.itdolan31.crystalpad.core.ui.theme.CrystalPadTheme
import com.git.itdolan31.crystalpad.features.app_lock.presentation.AppLockScreen
import com.git.itdolan31.crystalpad.navigation.CrystalPadNavHost
import com.git.itdolan31.crystalpad.navigation.Screen
import com.git.itdolan31.crystalpad.presentation.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !(viewModel.isThemeLoaded && viewModel.isDynamicColorLoaded && viewModel.isPasswordLoaded)
        }

        observeFlagSecure()
        enableEdgeToEdge()

        setContent {
            val backStack = rememberNavBackStack(Screen.Home)

            val themeType by viewModel.themeType.collectAsStateWithLifecycle()
            val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsStateWithLifecycle()
            val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()

            CrystalPadTheme(themeType = themeType, dynamicColor = dynamicColorEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isLocked) {
                        AppLockScreen(
                            onUnlocked = { viewModel.unlock() })
                    } else {
                        CrystalPadNavHost(backStack)
                    }
                }
            }
        }

        viewModel.cleanExpiredTrash()
    }

    override fun onStart() {
        super.onStart()

        viewModel.scheduleLockEnabled(false)
    }

    override fun onStop() {
        super.onStop()

        viewModel.scheduleLockEnabled(true)
    }

    private fun observeFlagSecure() {
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
}