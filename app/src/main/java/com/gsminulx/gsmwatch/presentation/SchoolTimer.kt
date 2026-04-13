// SchoolTimer.kt

package com.gsminulx.gsmwatch.presentation

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gsminulx.gsmwatch.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

// 데이터 클래스는 그대로 유지
data class Period(val name: String, val start: LocalTime, val end: LocalTime, val subject: String = "")
data class MealInfo(val type: String, val menu: String, val endTime: LocalTime)
data class TaskInfo(val id: Int, val type: String, val deadline: String, val content: String) // [추가] D-Day 일정 데이터


class SchoolTimer(val grade: Int = 1, val classNum: Int = 1, val sharedPref: SharedPreferences) {

    private val apiKey = BuildConfig.DATA_GSM_API_KEY
    private val botApiUrl = BuildConfig.BOT_API_URL // [추가] AWS 디스코드 봇 주소
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    // 기본 시간표 구조 (8~11교시 포함)
    val baseSchedule = listOf(
        Period("기상 시간", LocalTime.parse("06:30", formatter), LocalTime.parse("07:20", formatter)),
        Period("등교 시간", LocalTime.parse("07:20", formatter), LocalTime.parse("08:15", formatter)),
        Period("0교시", LocalTime.parse("08:15", formatter), LocalTime.parse("08:30", formatter)),
        Period("조회 및 준비", LocalTime.parse("08:30", formatter), LocalTime.parse("08:40", formatter)),
        Period("1교시", LocalTime.parse("08:40", formatter), LocalTime.parse("09:30", formatter)),
        Period("쉬는 시간", LocalTime.parse("09:30", formatter), LocalTime.parse("09:40", formatter)),
        Period("2교시", LocalTime.parse("09:40", formatter), LocalTime.parse("10:30", formatter)),
        Period("쉬는 시간", LocalTime.parse("10:30", formatter), LocalTime.parse("10:40", formatter)),
        Period("3교시", LocalTime.parse("10:40", formatter), LocalTime.parse("11:30", formatter)),
        Period("쉬는 시간", LocalTime.parse("11:30", formatter), LocalTime.parse("11:40", formatter)),
        Period("4교시", LocalTime.parse("11:40", formatter), LocalTime.parse("12:30", formatter)),
        Period("점심 시간", LocalTime.parse("12:30", formatter), LocalTime.parse("13:30", formatter)),
        Period("5교시", LocalTime.parse("13:30", formatter), LocalTime.parse("14:20", formatter)),
        Period("쉬는 시간", LocalTime.parse("14:20", formatter), LocalTime.parse("14:30", formatter)),
        Period("6교시", LocalTime.parse("14:30", formatter), LocalTime.parse("15:20", formatter)),
        Period("쉬는 시간", LocalTime.parse("15:20", formatter), LocalTime.parse("15:30", formatter)),
        Period("7교시", LocalTime.parse("15:30", formatter), LocalTime.parse("16:20", formatter)),
        Period("청소 및 종례", LocalTime.parse("16:20", formatter), LocalTime.parse("16:40", formatter)),
        Period("8교시", LocalTime.parse("16:40", formatter), LocalTime.parse("17:30", formatter)),
        Period("쉬는 시간", LocalTime.parse("17:30", formatter), LocalTime.parse("17:40", formatter)),
        Period("9교시", LocalTime.parse("17:40", formatter), LocalTime.parse("18:30", formatter)),
        Period("석식 시간", LocalTime.parse("18:30", formatter), LocalTime.parse("19:30", formatter)),
        Period("10교시", LocalTime.parse("19:30", formatter), LocalTime.parse("20:20", formatter)),
        Period("쉬는 시간", LocalTime.parse("20:20", formatter), LocalTime.parse("20:30", formatter)),
        Period("11교시", LocalTime.parse("20:30", formatter), LocalTime.parse("21:20", formatter)),
        Period("기숙사 입소", LocalTime.parse("21:20", formatter), LocalTime.parse("21:30", formatter)),
        Period("점호", LocalTime.parse("22:00", formatter), LocalTime.parse("22:10", formatter))
    )

    val weekdays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)

    var weeklyTimetable by mutableStateOf<Map<DayOfWeek, List<Period>>>(emptyMap())
    var weeklyMeals by mutableStateOf<Map<DayOfWeek, List<MealInfo>>>(emptyMap())
    var ddayTasks by mutableStateOf<List<TaskInfo>>(emptyList()) // [추가] D-Day 과제 목록 상태

    // 커스텀 과목 저장/삭제 함수
    fun saveCustomSubject(day: DayOfWeek, period: Int, subject: String) {
        sharedPref.edit().putString("custom_${day.name}_$period", subject).apply()
    }

    fun clearCustomSubject(day: DayOfWeek, period: Int) {
        sharedPref.edit().remove("custom_${day.name}_$period").apply()
    }

    suspend fun fetchRealData() = withContext(Dispatchers.IO) {
        try {
            var targetDate = LocalDate.now()
            if (isNextWeekTarget()) targetDate = targetDate.plusDays(3)

            val monday = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val friday = monday.plusDays(4)
            val fmt = DateTimeFormatter.ofPattern("yyyyMMdd")
            val fromDate = monday.format(fmt)
            val toDate = friday.format(fmt)

            val mealUrl = "https://open.neis.go.kr/hub/mealServiceDietInfo?KEY=$apiKey&Type=json&pSize=1000&ATPT_OFCDC_SC_CODE=F10&SD_SCHUL_CODE=7380292&MLSV_FROM_YMD=$fromDate&MLSV_TO_YMD=$toDate"
            val timeUrl = "https://open.neis.go.kr/hub/hisTimetable?KEY=$apiKey&Type=json&pSize=1000&ATPT_OFCDC_SC_CODE=F10&SD_SCHUL_CODE=7380292&GRADE=$grade&CLASS_NM=$classNum&TI_FROM_YMD=$fromDate&TI_TO_YMD=$toDate"

            val mealJson = try { URL(mealUrl).readText() } catch (e: Exception) { sharedPref.getString("cached_meal", "") ?: "" }
            val timeJson = try { URL(timeUrl).readText() } catch (e: Exception) { sharedPref.getString("cached_time", "") ?: "" }
            val tasksJson = try { URL("$botApiUrl/api/tasks?grade=$grade&class_nm=$classNum").readText() } catch (e: Exception) { sharedPref.getString("cached_tasks", "[]") ?: "[]" }

            if (mealJson.isNotEmpty()) sharedPref.edit().putString("cached_meal", mealJson).apply()
            if (timeJson.isNotEmpty()) sharedPref.edit().putString("cached_time", timeJson).apply()
            if (tasksJson.isNotBlank() && tasksJson != "[]") sharedPref.edit().putString("cached_tasks", tasksJson).apply()

            val parsedMeals = parseMeals(mealJson)
            val parsedTimetable = parseTimetable(timeJson)
            val parsedTasks = parseTasks(tasksJson) // [추가]

            withContext(Dispatchers.Main) {
                weeklyMeals = parsedMeals
                weeklyTimetable = parsedTimetable
                ddayTasks = parsedTasks // [추가]
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun parseTimetable(json: String): Map<DayOfWeek, List<Period>> {
        val parsedData = mutableMapOf<DayOfWeek, MutableMap<Int, String>>()
        if (json.contains("\"hisTimetable\"")) {
            val rows = JSONObject(json).getJSONArray("hisTimetable").getJSONObject(1).getJSONArray("row")
            for (i in 0 until rows.length()) {
                val row = rows.getJSONObject(i)
                val date = LocalDate.parse(row.optString("ALL_TI_YMD"), DateTimeFormatter.ofPattern("yyyyMMdd"))
                val p = row.optString("PERIO").replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                val content = row.optString("ITRT_CNTNT", "")
                if (p > 0 && content.isNotEmpty()) {
                    parsedData.getOrPut(date.dayOfWeek) { mutableMapOf() }[p] = content
                }
            }
        }

        val finalMap = mutableMapOf<DayOfWeek, List<Period>>()
        for (day in weekdays) {
            val dailySubjects = parsedData[day] ?: emptyMap()
            finalMap[day] = baseSchedule.map { period ->
                if (period.name.contains("교시")) {
                    val pNum = period.name.replace("교시", "").toIntOrNull() ?: 0
                    // 저장된 커스텀 과목이 있으면 우선 적용
                    val customSubj = sharedPref.getString("custom_${day.name}_${pNum}", null)
                    val realSubj = customSubj ?: dailySubjects[pNum]
                    if (realSubj != null) period.copy(subject = realSubj) else period
                } else period
            }
        }
        return finalMap
    }

    // ... 기타 헬퍼 함수들 (isWeekend, getTodaySchedule 등) 유지 ...
    fun isWeekend(): Boolean {
        val today = LocalDate.now().dayOfWeek
        return today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY
    }

    private fun isNextWeekTarget(): Boolean {
        val now = LocalDateTime.now()
        return isWeekend() || (now.dayOfWeek == DayOfWeek.FRIDAY && now.toLocalTime().isAfter(LocalTime.of(19, 30)))
    }

    fun getTodaySchedule(): List<Period> = weeklyTimetable[LocalDate.now().dayOfWeek] ?: baseSchedule

    fun getVisibleScheduleForDay(day: DayOfWeek): List<Period> {
        val schedule = weeklyTimetable[day] ?: baseSchedule
        return schedule.filter { period ->
            val isTarget = period.name.contains("교시")
            val isHidden = period.name in listOf("0교시", "10교시", "11교시")
            val isFridayHidden = day == DayOfWeek.FRIDAY && period.name in listOf("8교시", "9교시")
            isTarget && !isHidden && !isFridayHidden
        }
    }

    fun getDayName(day: DayOfWeek) = when (day) {
        DayOfWeek.MONDAY -> "월요일"; DayOfWeek.TUESDAY -> "화요일"; DayOfWeek.WEDNESDAY -> "수요일"
        DayOfWeek.THURSDAY -> "목요일"; DayOfWeek.FRIDAY -> "금요일"; else -> "주말"
    }

    fun getRemainingTimeMessage(): String {
        val nowDateTime = LocalDateTime.now()
        val nowTime = nowDateTime.toLocalTime()
        val today = nowDateTime.dayOfWeek

        if (today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY) {
            var targetDate = nowDateTime.toLocalDate()
            while (targetDate.dayOfWeek != DayOfWeek.SUNDAY) { targetDate = targetDate.plusDays(1) }
            val targetTime = when (grade) { 1 -> LocalTime.of(20, 20); 2 -> LocalTime.of(20, 40); else -> LocalTime.of(21, 0) }
            val targetDateTime = LocalDateTime.of(targetDate, targetTime)
            if (nowDateTime.isAfter(targetDateTime)) return "기숙사 입소 완료!\n새로운 주를 준비하세요 🌙"
            val duration = Duration.between(nowDateTime, targetDateTime)
            val h = duration.toHours()
            val m = String.format("%02d", duration.toMinutes() % 60)
            val s = String.format("%02d", duration.seconds % 60)
            return "기숙사 입소까지\n${h}시간 ${m}분 ${s}초"
        }

        val schedule = getTodaySchedule()
        val currentPeriod = schedule.find { (nowTime.isAfter(it.start) || nowTime == it.start) && nowTime.isBefore(it.end) }
        return if (currentPeriod != null) {
            val duration = Duration.between(nowTime, currentPeriod.end)
            val displayName = if (currentPeriod.subject.isNotEmpty()) "${currentPeriod.name} ${currentPeriod.subject}" else currentPeriod.name

            // %02d를 쓰면 '9초'도 '09초'가 되어서 글자 너비가 변하지 않아!
            val m = String.format("%02d", duration.toMinutes())
            val s = String.format("%02d", duration.seconds % 60)
            "$displayName 종료까지\n${m}분 ${s}초" // \n으로 무조건 2줄 유지
        } else "현재는 정규 일과\n시간이 아닙니다."
    }

    fun getUpcomingMeals(): List<Pair<String, MealInfo>> {
        if (weeklyMeals.isEmpty()) return emptyList()
        val nowTime = LocalTime.now()
        val today = LocalDate.now().dayOfWeek

        // 👉 [수정된 부분] Pair(day, meal = it) 을 Pair(day, it)으로 수정했어!
        val allMeals = weekdays.flatMap { day -> (weeklyMeals[day] ?: emptyList()).map { Pair(day, it) } }

        var startIndex = allMeals.indexOfFirst { (mealDay, meal) -> mealDay > today || (mealDay == today && meal.endTime.isAfter(nowTime)) }
        if (startIndex == -1) startIndex = 0

        return (0 until minOf(3, allMeals.size)).map { i ->
            val data = allMeals[(startIndex + i) % allMeals.size]
            val title = when {
                data.first == today -> data.second.type
                data.first == today.plus(1) -> "내일 ${data.second.type}"
                else -> "${getDayName(data.first)} ${data.second.type}"
            }
            Pair(title, data.second)
        }
    }

    private fun parseMeals(json: String): Map<DayOfWeek, List<MealInfo>> {
        val map = mutableMapOf<DayOfWeek, MutableList<MealInfo>>()
        if (!json.contains("\"mealServiceDietInfo\"")) return map
        val rows = JSONObject(json).getJSONArray("mealServiceDietInfo").getJSONObject(1).getJSONArray("row")
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            val date = LocalDate.parse(row.optString("MLSV_YMD"), DateTimeFormatter.ofPattern("yyyyMMdd"))
            val type = row.optString("MMEAL_SC_NM").replace(" ", "")
            val menu = row.optString("DDISH_NM").replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n").replace(Regex("\\([0-9.]+\\)"), "").replace(Regex("[0-9]+\\."), "").trim()
            val endTime = when(type) { "조식" -> LocalTime.of(8, 40); "중식" -> LocalTime.of(13, 30); "석식" -> LocalTime.of(19, 30); else -> LocalTime.of(23, 59) }
            map.getOrPut(date.dayOfWeek) { mutableListOf() }.add(MealInfo(type, menu, endTime))
        }
        return map
    }

    // [추가] 통신을 통해 가져온 D-Day JSON 데이터 파싱 함수
    private fun parseTasks(json: String): List<TaskInfo> {
        val list = mutableListOf<TaskInfo>()
        if (json.isBlank() || json == "[]" || json.contains("\"error\"")) return list
        try {
            val array = org.json.JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(TaskInfo(
                    id = obj.optInt("id", 0),
                    type = obj.optString("task_type", ""),
                    deadline = obj.optString("deadline", "미정"),
                    content = obj.optString("content", "")
                ))
            }
        } catch (e: Exception) { e.printStackTrace() }
        return list
    }
}
