package com.gsminulx.gsmwatch.data

import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.JsonObject

// [개선 1] Retrofit을 이용한 효율적인 API 통신 인터페이스
interface NeisApi {
    @GET("hub/mealServiceDietInfo")
    suspend fun getMeals(
        @Query("KEY") key: String = Constants.API_KEY,
        @Query("Type") type: String = "json",
        @Query("pSize") pSize: Int = 1000,
        @Query("ATPT_OFCDC_SC_CODE") officeCode: String = Constants.ATPT_OFCDC_SC_CODE,
        @Query("SD_SCHUL_CODE") schoolCode: String = Constants.SD_SCHUL_CODE,
        @Query("MLSV_FROM_YMD") fromDate: String,
        @Query("MLSV_TO_YMD") toDate: String
    ): JsonObject

    @GET("hub/hisTimetable")
    suspend fun getTimetable(
        @Query("KEY") key: String = Constants.API_KEY,
        @Query("Type") type: String = "json",
        @Query("pSize") pSize: Int = 1000,
        @Query("ATPT_OFCDC_SC_CODE") officeCode: String = Constants.ATPT_OFCDC_SC_CODE,
        @Query("SD_SCHUL_CODE") schoolCode: String = Constants.SD_SCHUL_CODE,
        @Query("GRADE") grade: Int,
        @Query("CLASS_NM") classNum: Int,
        @Query("TI_FROM_YMD") fromDate: String,
        @Query("TI_TO_YMD") toDate: String
    ): JsonObject
}