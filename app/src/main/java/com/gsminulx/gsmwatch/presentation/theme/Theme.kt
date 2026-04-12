package com.gsminulx.gsmwatch.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

// Refined AMOLED Neon Theme (프리미엄 네온 시안 & 딥 블랙 테마)
val PrimaryCyan = Color(0xFF00E5FF)        // 강렬하고 트렌디한 네온 시안
val OnPrimary = Color(0xFF003333)          // 대비감을 높인 짙은 청록색 (텍스트용)
val PrimaryContainer = Color(0xFF0D47A1)   // 네온을 돋보이게 하는 깊이 있는 딥 블루 네이비
val OnPrimaryContainer = Color(0xFFE3F2FD) 

val SurfaceDark = Color(0xFF000000)        // 완벽한 배터리 절약형 AMOLED Black
val SurfaceContainerDark = Color(0xFF131318) // 너무 까맣지 않은 아주 미세한 네이비 틴트 블랙 (유리 질감 느낌)
val OnSurfaceDark = Color(0xFFF0F0F0)      // 밝고 깨끗한 본문용 흰색
val OnSurfaceVariantDark = Color(0xFF9AA0A6)// 대비를 살려주는 차분한 실버/그레이

val ErrorNeon = Color(0xFFFF1744)          // 확실하게 사용자 시선을 끄는 형광 레드
val OnError = Color(0xFF410002)

val GsmWatchColorScheme = ColorScheme(
    primary = PrimaryCyan,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    surfaceContainer = SurfaceContainerDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    error = ErrorNeon,
    onError = OnError,
    // 프리미엄 디자인 확장을 위한 추가 속성 대비
)

@Composable
fun GsmWatchTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GsmWatchColorScheme,
        // (향후 타이포그래피 별도 적용 가능)
        content = content
    )
}