package ir.ghias.alimaa.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ir.ghias.alimaa.core.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhiyasTopAppBar(
    onMenuClick: () -> Unit,
    onClearClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text = "قیاس", color = Color.White) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onClearClick) {
                Icon(
                    imageVector = Icons.Default.ClearAll,
                    contentDescription = "Clear Form",
                    tint = Color.White
                )
            }
            IconButton(onClick = onHistoryClick) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GreenPrimary
        )
    )
}
