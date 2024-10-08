package com.jzheng23.quicklock

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var overlayPermissionGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var accessibilityServiceEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    var showAccessibilityDialog by remember { mutableStateOf(false) }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        overlayPermissionGranted = Settings.canDrawOverlays(context)
    }

    val accessibilitySettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        accessibilityServiceEnabled = isAccessibilityServiceEnabled(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quick Lock",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.permission_description),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        PermissionSwitch(
            text = "Overlay Permission",
            checked = overlayPermissionGranted,
            onCheckedChange = { checked ->
                if (checked) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    overlayPermissionLauncher.launch(intent)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionSwitch(
            text = "Accessibility Service",
            checked = accessibilityServiceEnabled,
            onCheckedChange = { checked ->
                if (checked) {
                    showAccessibilityDialog = true
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (overlayPermissionGranted && accessibilityServiceEnabled) {
                    context.startService(Intent(context, OverlayService::class.java))
                    // You might want to finish the activity here if needed
                    val activity = context as? Activity
                    activity?.moveTaskToBack(true)
                }
            },
            enabled = overlayPermissionGranted && accessibilityServiceEnabled
        ) {
            Text("Start the floating button")
        }
    }

    if (showAccessibilityDialog) {
        AlertDialog(
            onDismissRequest = { showAccessibilityDialog = false },
            title = { Text("Accessibility Service") },
            text = { Text("To enable this app to lock the screen, you need to allow the accessibility service. Would you like to open the settings now?") },
            confirmButton = {
                Button(onClick = {
                    showAccessibilityDialog = false
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    accessibilitySettingsLauncher.launch(intent)
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                Button(onClick = { showAccessibilityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PermissionSwitch(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// Helper function to check if the accessibility service is enabled
fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
    val accessibilityEnabled = Settings.Secure.getInt(
        context.contentResolver,
        Settings.Secure.ACCESSIBILITY_ENABLED, 0
    )
    if (accessibilityEnabled == 1) {
        val service = "${context.packageName}/${ScreenLockService::class.java.canonicalName}"
        val settingValue = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return settingValue?.contains(service) == true
    }
    return false
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
