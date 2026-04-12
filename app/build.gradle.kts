import java.util.Properties // 추가됨: Properties 클래스 임포트
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.gsminulx.gsmwatch"

    // 36 버전 경고가 뜨더라도 무시하시고 안정적인 35 버전을 쓰시는 것을 권장합니다.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gsminulx.gsmwatch"
        minSdk = 30
        targetSdk = 36

        versionCode = 11
        versionName = "0.6.1"

        // 👉 [수정] 혹시 모를 따옴표(")를 강제로 모두 지워버린 후 감싸기!
        val dataGsmApiKey = (localProperties.getProperty("DATA_GSM_API_KEY") ?: "").replace("\"", "")
        buildConfigField("String", "DATA_GSM_API_KEY", "\"$dataGsmApiKey\"")

        // 👉 [수정] 여기도 마찬가지!
        val gsmApiKey = (localProperties.getProperty("GSM_API_KEY") ?: "").replace("\"", "")
        buildConfigField("String", "GSM_API_KEY", "\"$gsmApiKey\"")

        // 👉 [추가] D-Day 데이터를 받아오기 위한 디스코드 봇 서버 API 주소
        val botApiUrl = (localProperties.getProperty("BOT_API_URL") ?: "http://127.0.0.1:8080").replace("\"", "")
        buildConfigField("String", "BOT_API_URL", "\"$botApiUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // 에러를 유발했던 kotlinOptions 블록 삭제 완료

    useLibrary("wear-sdk")

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.tooling)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    // [개선 1, 2] 네트워크 및 JSON 파싱 (Retrofit + Gson)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // [개선 5] ViewModel 및 Lifecycle (MVVM 패턴)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // [개선 8] SharedPreferences 대체용 DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // [개선 11] Wear OS Ambient Mode (AOD) 지원
    implementation("androidx.wear.compose:compose-foundation:1.2.1")
    implementation("androidx.wear.compose:compose-navigation:1.2.1")
}
