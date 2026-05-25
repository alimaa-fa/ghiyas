package ir.ghias.alimaa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.ghias.alimaa.core.theme.BackgroundGray
import ir.ghias.alimaa.core.theme.QiyasTheme
import ir.ghias.alimaa.core.theme.SurfaceCard
import ir.ghias.alimaa.presentation.components.GhiyasTopAppBar
import ir.ghias.alimaa.presentation.components.HeroBanner
import ir.ghias.alimaa.presentation.stages.input.InputStageScreen
import ir.ghias.alimaa.presentation.stages.input.InputStageViewModel

@Composable
fun App() {
    QiyasTheme {
        // راست‌چین کردن کل اپلیکیشن در بالاترین سطح
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundGray),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxSize()
                        .background(SurfaceCard),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val viewModel: InputStageViewModel = viewModel { InputStageViewModel() }

                    Scaffold(
                        topBar = {
                            GhiyasTopAppBar(
                                onMenuClick = { /* TODO */ },
                                onClearClick = { viewModel.clearForm() },
                                onHistoryClick = { /* TODO */ }
                            )
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(SurfaceCard)
                        ) {
                            HeroBanner()
                            InputStageScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
