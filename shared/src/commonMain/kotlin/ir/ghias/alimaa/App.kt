package ir.ghias.alimaa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ghias.alimaa.core.theme.BackgroundGray
import ir.ghias.alimaa.core.theme.QiyasTheme
import ir.ghias.alimaa.core.theme.SurfaceCard

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
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "پروژه قیاس - نسخه وب - راه‌اندازی موفق",
                        color = Color.Black,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}
