package com.gsminulx.gsmwatch.presentation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import androidx.wear.compose.material3.*

@Composable
fun TimerScreen(grade: Int, classNum: Int, onTimetableClick: () -> Unit, onMealsClick: () -> Unit, onTasksClick: () -> Unit, onSettingsClick: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("GsmWatchPrefs", Context.MODE_PRIVATE) }

    val timer = remember(grade, classNum) { SchoolTimer(grade, classNum, sharedPref) }
    var timeMessage by remember { mutableStateOf(timer.getRemainingTimeMessage()) }

    // 👉 최신 방식으로 LifecycleOwner 가져오기
    val lifecycleOwner = LocalLifecycleOwner.current

    // 👉 화면이 켜져 있을 때만 1초마다 업데이트 (배터리 절약의 핵심!)
    LaunchedEffect(timer, lifecycleOwner) {
        launch { timer.fetchRealData() }

        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                timeMessage = timer.getRemainingTimeMessage()
                delay(1000L)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 20.dp, bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                CompactButton(
                    onClick = onSettingsClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
                ) {
                    Text("⚙️ ${grade}학년 ${classNum}반", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
            item {
                Text(
                    text = timeMessage,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 19.sp, // 더 크게
                    fontWeight = FontWeight.Black, // 가장 두껍게
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontFeatureSettings = "tnum"), 
                    modifier = Modifier.padding(bottom = 20.dp, start = 10.dp, end = 10.dp)
                )
            }

            if (!timer.isWeekend()) {
                val today = LocalDate.now().dayOfWeek
                val displaySchedule = timer.getVisibleScheduleForDay(today).filter { period ->
                    period.subject.isNotEmpty()
                }
                item { ScheduleCard(displaySchedule) }
                item { 
                    CompactButton(onClick = onTimetableClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer, contentColor = MaterialTheme.colorScheme.primary), modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)) {
                        Text("시간표 상세정보", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            val upcomingMeals = timer.getUpcomingMeals()
            if (upcomingMeals.isEmpty()) {
                item { Text("식단 로딩중...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(vertical = 16.dp)) }
            } else {
                itemsIndexed(upcomingMeals) { index, (title, mealInfo) ->
                    val isHighlight = index == 0
                    MealCard(title = title, menu = mealInfo.menu, isHighlight = isHighlight)
                }
                item { 
                    CompactButton(onClick = onMealsClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer, contentColor = MaterialTheme.colorScheme.primary), modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)) {
                        Text("식단표 상세정보", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            
            item { 
                Button(
                    onClick = onTasksClick, 
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer), 
                    modifier = Modifier.fillMaxWidth(0.9f).padding(top = 8.dp)
                ) {
                    Text("💡 학급 일정 & D-Day", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
