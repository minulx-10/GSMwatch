package com.gsminulx.gsmwatch.data

import androidx.compose.runtime.Immutable
import java.time.LocalTime

// [개선 4] @Immutable을 추가하여 Compose의 불필요한 재구성(Recomposition) 방지
@Immutable
data class Period(
    val name: String,
    val start: LocalTime,
    val end: LocalTime,
    val subject: String = ""
)

@Immutable
data class MealInfo(
    val type: String,
    val menu: String,
    val endTime: LocalTime
)

// [개선 9] 네트워크 상태 및 에러 핸들링을 위한 Result 래퍼
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}