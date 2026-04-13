package com.gsminulx.gsmwatch.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.gsminulx.gsmwatch.presentation.theme.GsmWatchTheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.gsminulx.gsmwatch.analytics.AppAnalytics

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // [수정] 스플래시 스크린 적용 (튕김 현상 방지)
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            // [수정] GsmWatchTheme으로 감싸서 정의한 다크 테마 색상이 정상 출력되도록 함
            GsmWatchTheme {
                MainApp()
            }
        }
    }
}

enum class AppScreen { MAIN, WEEKLY_TIMETABLE, WEEKLY_MEALS, DDAY_TASKS, SETTINGS } // [수정] DDAY_TASKS 추가

@Composable
fun MainApp() {
    val context = LocalContext.current
    val analytics = remember(context) { AppAnalytics(context) }
    // [수정] 설정값을 기기에 영구 저장하기 위한 SharedPreferences
    val sharedPref = context.getSharedPreferences("GsmWatchPrefs", Context.MODE_PRIVATE)
    var userGrade by remember { mutableIntStateOf(sharedPref.getInt("grade", 1)) }
    var userClass by remember { mutableIntStateOf(sharedPref.getInt("classNum", 1)) }
    val navController = rememberSwipeDismissableNavController()

    LaunchedEffect(Unit) {
        analytics.trackScreen("main")
    }

    // 👉 [핵심 2] 스와이프 시 자연스럽게 이전 화면으로 돌아가는 NavHost
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = AppScreen.MAIN.name
    ) {

        // 1. 메인 타이머 화면
        composable(AppScreen.MAIN.name) {
            TimerScreen(
                grade = userGrade,
                classNum = userClass,
                onTimetableClick = {
                    analytics.trackScreen("weekly_timetable")
                    navController.navigate(AppScreen.WEEKLY_TIMETABLE.name)
                },
                onMealsClick = {
                    analytics.trackScreen("weekly_meals")
                    navController.navigate(AppScreen.WEEKLY_MEALS.name)
                },
                onTasksClick = { // [추가]
                    analytics.trackScreen("dday_tasks")
                    navController.navigate(AppScreen.DDAY_TASKS.name)
                },
                onSettingsClick = {
                    analytics.trackScreen("settings")
                    navController.navigate(AppScreen.SETTINGS.name)
                }
            )
        }

        // 2. 시간표 전체보기 화면
        composable(AppScreen.WEEKLY_TIMETABLE.name) {
            WeeklyTimetableScreen(
                grade = userGrade,
                classNum = userClass,
                onBack = { navController.popBackStack() } // 👉 스와이프하거나 '돌아가기'를 누르면 스택에서 빠짐
            )
        }

        // 3. 식단표 전체보기 화면
        composable(AppScreen.WEEKLY_MEALS.name) {
            WeeklyMealsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // [추가] 4. D-Day 일정 목록 화면
        composable(AppScreen.DDAY_TASKS.name) {
            DDayTasksScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 4. 설정 화면
        composable(AppScreen.SETTINGS.name) {
            SettingsScreen(
                initialGrade = userGrade,
                initialClass = userClass,
                onSave = { newGrade, newClass ->
                    sharedPref.edit().putInt("grade", newGrade).putInt("classNum", newClass).apply()
                    userGrade = newGrade
                    userClass = newClass
                    analytics.trackSettingsSaved(newGrade, newClass)
                    navController.popBackStack() // 👉 저장 완료 시 이전 화면(메인)으로 돌아감
                }
            )
        }
    }
}
