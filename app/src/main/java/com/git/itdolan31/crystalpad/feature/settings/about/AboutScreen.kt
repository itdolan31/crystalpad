package com.git.itdolan31.crystalpad.feature.settings.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.feature.settings.components.SettingsTextItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit, onNavigateToLicenses: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, title = { Text(stringResource(R.string.about_title)) })
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState, modifier = Modifier
                    .padding(8.dp, 0.dp)
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null
                )
                Text("Crystalpad", style = MaterialTheme.typography.headlineLarge)
            }

            SettingsTextItem(
                onClick = { uriHandler.openUri("https://github.com/itdolan31/crystalpad") },
                title = stringResource(R.string.source_code)
            )

            SettingsTextItem(
                onClick = onNavigateToLicenses, title = stringResource(R.string.licenses_title)
            )

            var versionTapCount by remember { mutableIntStateOf(0) }
            var lastVersionTap by remember { mutableLongStateOf(0L) }

            SettingsTextItem(
                onClick = {
                    val now = System.currentTimeMillis()
                    if (now - lastVersionTap > 2000) versionTapCount = 0
                    lastVersionTap = now
                    versionTapCount++
                    if (versionTapCount >= 7) {
                        versionTapCount = 0
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.hello))
                        }
                    }
                }, title = stringResource(
                    R.string.version, "${
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName
                    }"
                )
            )
        }
    }
}