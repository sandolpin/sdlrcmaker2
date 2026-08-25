package com.sandolpin.sdlrcmaker2.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sandolpin.sdlrcmaker2.data.model.PlayerActionButton
import com.sandolpin.sdlrcmaker2.ui.theme.DarkModeOption
import com.sandolpin.sdlrcmaker2.util.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val darkModeOption by viewModel.darkModeOption.collectAsState()
    val visibleButtons by viewModel.visiblePlayerButtons.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("設定") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("ダークモード", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                DarkModeOption.entries.forEach { option ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(
                            selected = darkModeOption == option,
                            onClick = { viewModel.setDarkModeOption(option) },
                        )
                        Text(option.label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("編集画面", style = MaterialTheme.typography.labelLarge)
            Text(
                "メディアプレイヤー下のボタンの表示",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                PlayerActionButton.entries.forEach { button ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(
                            checked = button in visibleButtons,
                            onCheckedChange = { viewModel.toggleButtonVisibility(button) },
                        )
                        Text(button.label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            NotificationAccessSection(context)
        }
    }
}

@Composable
private fun NotificationAccessSection(context: Context) {
    var granted by remember { mutableStateOf(PermissionUtils.isNotificationAccessGranted(context)) }

    Text("メディア連携", style = MaterialTheme.typography.labelLarge)
    Text(
        "再生中の曲を検知するには通知へのアクセスを許可してください。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    )
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = {
        context.startActivity(PermissionUtils.notificationAccessSettingsIntent())
        granted = PermissionUtils.isNotificationAccessGranted(context)
    }) {
        Text(if (granted) "許可済み(設定を開く)" else "通知へのアクセスを許可する")
    }
}
