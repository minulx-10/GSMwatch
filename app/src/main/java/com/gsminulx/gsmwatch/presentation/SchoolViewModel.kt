package com.gsminulx.gsmwatch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gsminulx.gsmwatch.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

// [개선 5] MVVM 패턴 적용: 상태 관리와 비즈니스 로직을 ViewModel로 이동
class SchoolViewModel : ViewModel() {

    private val _timeMessage = MutableStateFlow("계산 중...")
    val timeMessage: StateFlow<String> = _timeMessage.asStateFlow()

    // 상태 관리 (로딩, 성공, 실패)
    private val _scheduleState = MutableStateFlow<UiState<List<Period>>>(UiState.Loading)
    val scheduleState = _scheduleState.asStateFlow()

    // [개선 10] 생명주기를 인지하는 타이머 시작 함수 (UI에서 화면이 보일 때만 호출)
    fun startTimer(isAmbientMode: Boolean) {
        viewModelScope.launch {
            while (true) {
                updateRemainingTime()
                // [개선 11] AOD(Ambient) 모드일 경우 1분이 아닌 60초마다 갱신하여 배터리 절약
                val delayTime = if (isAmbientMode) 60000L else 1000L
                delay(delayTime)
            }
        }
    }

    private fun updateRemainingTime() {
        // 기존 getRemainingTimeMessage()의 로직을 이곳에 배치합니다.
        // 데이터가 업데이트 되면 _timeMessage.value 에 값을 할당합니다.
        val nowDateTime = LocalDateTime.now()
        // ... (기존 SchoolTimer.kt의 시간 계산 로직) ...
        _timeMessage.value = "기숙사 입소까지\n... (계산된 시간)"
    }

    // 네트워크 데이터 Fetch 로직 (Retrofit Repository 호출)
    fun fetchData(grade: Int, classNum: Int) {
        viewModelScope.launch {
            _scheduleState.value = UiState.Loading
            try {
                // Repository를 통해 데이터를 가져오고 성공 시 Success 배출
                // (이곳에서 네이스 API 연동 로직 호출)
            } catch (e: Exception) {
                // [개선 9] 에러 발생 시 UI에 명확히 알림
                _scheduleState.value = UiState.Error("네트워크 오류: 캐시된 데이터를 사용합니다.")
            }
        }
    }
}