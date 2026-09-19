package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.NovaMainViewModel
import com.example.ui.components.NovaBottomNav
import com.example.ui.components.NovaTab
import com.example.ui.screens.AutomationScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.FilesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaceDarkBg

class MainActivity : ComponentActivity() {

    private val viewModel: NovaMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleVoiceTriggerIntent(intent)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleVoiceTriggerIntent(intent)
    }

    private fun handleVoiceTriggerIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("TRIGGER_VOICE_IMMEDIATELY", false) == true ||
            intent?.getBooleanExtra("TOGGLE_VOICE_IMMEDIATELY", false) == true) {
            viewModel.toggleVoiceListening()
        }
    }
}

@Composable
fun MainAppContent(viewModel: NovaMainViewModel) {
    var selectedTab by remember { mutableStateOf(NovaTab.HUB) }
    val showOnboarding by viewModel.showOnboarding.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Handled */ }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        containerColor = SpaceDarkBg,
        bottomBar = {
            NovaBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTab) {
                NovaTab.HUB -> HomeScreen(viewModel = viewModel)
                NovaTab.CHAT -> ChatScreen(viewModel = viewModel)
                NovaTab.AUTOMATION -> AutomationScreen(viewModel = viewModel)
                NovaTab.FILES -> FilesScreen(viewModel = viewModel)
                NovaTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }

            if (showOnboarding) {
                OnboardingDialog(
                    initialApiKey = viewModel.preferencesManager.getApiKey(),
                    initialLanguage = viewModel.preferencesManager.primaryLanguage,
                    onComplete = { apiKey, language ->
                        viewModel.completeOnboarding(apiKey, language)
                    }
                )
            }
        }
    }
}

