package ir.ghias.alimaa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp

@Composable
fun App() {
    MaterialTheme {
        // راست‌چین کردن کل اپلیکیشن در بالاترین سطح
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF4CAF50)), // تم سبز سازمانی
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "پروژه قیاس - نسخه وب - راه‌اندازی موفق",
                    color = Color.White,
                    fontSize = 24.sp
                )
            }
        }
    }
}
