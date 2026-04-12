<h1 align="center">
  ⌚ GSMwatch
</h1>

<p align="center">
  <a href="https://github.com/minulx-10/gsmwatch/blob/main/LICENSE"><img loading="lazy" src="https://img.shields.io/github/license/minulx-10/gsmwatch?style=for-the-badge&logo=github"/></a>
  <a href="https://github.com/minulx-10/gsmwatch/releases"><img loading="lazy" src="https://img.shields.io/github/v/release/minulx-10/gsmwatch?include_prereleases&style=for-the-badge"/></a>
  <a href="https://www.android.com/wear-os/"><img loading="lazy" src="https://img.shields.io/badge/Platform-Wear%20OS-4285F4?style=for-the-badge&logo=android&logoColor=white"/></a>
</p>

<p align="center">광주소프트웨어마이스터고등학교 학생들을 위해 설계된 <b>프리미엄 Wear OS 애플리케이션</b>입니다.</p>

---

# 📖 Top-Down 개발 및 기획 의도 (Project Overview)

**"작은 손목 위에서, 가장 빠르고 직관적으로 일과를 확인하다."**

GSMwatch는 소프트웨어마이스터고등학교(GSM) 학생들의 효율적인 시간 관리를 위해 기획되었습니다. 단순히 스마트폰의 기능을 복제하는 것이 아닙니다. 저희는 스마트폰을 꺼낼 수 없는 상황이거나 즉각적인 알림이 필요한 순간, 마이크로 인터렉션의 혁신을 목표로 Wear OS 전용 앱으로 개발을 진행했습니다.

### 🎯 주요 해결 과제 (Problem Solving)
1. **수업 시간과 일과 휴식 타이머 제공:** 기존 시간표 앱은 단조로운 정보만 줍니다. GSMwatch는 분/초 단위로 계산하여 다음 일과까지 얼마나 남았는지 직관적으로 카운트다운 합니다. (예: 기상, 수업 종료, 식사 시간 등)
2. **복잡함 제거 (Zero Configuration):** 앱을 열면 **아무것도 조작할 필요 없는 상태**를 제공합니다. 스와이프 제스처만으로 시간표와 식단, D-Day를 넘어갑니다.

---

# 🛠 아키텍처 및 기술 스택 (Architecture & Tech Stack)

**1. Language & UI Framework**
* **Kotlin**: 간결하고 안전한 선언형 UI를 작성하기 위해 사용.
* **Jetpack Compose for Wear OS**: UI를 `ScalingLazyColumn`, `SwipeDismissableNavHost` 등 컴포즈 기반으로 작성해 원형 스마트워치 화면에 완벽히 최적화했습니다.

**2. Design System (Aesthetic UI)**
* **AMOLED 최적화 & Neon Cyan**: 완벽한 Black(`#000000`) 배경을 기반으로 작동하여 배터리 소모를 극적으로 낮춥니다. 핵심 정보는 트렌디한 형광색 `Neon Cyan (#00E5FF)`을 사용하여 고급스러운 퓨처리스틱 컨셉을 완성했습니다.

* **NEIS API**: 공공데이터포털 원격 서버와 HTTP 통신을 맺어 동적으로 급식 메뉴와 시간표를 가져옵니다.

---

# 🚀 주요 기능 및 백그라운드 로직 (Core Features)

### 1) 지능형 스케줄러 (Smart Time Computation)
`java.time` 라이브러리를 활용해 현재 시간을 측정합니다. 만약 금요일 저녁(석식 종료 이후)이거나 주말일 경우, 단말기 시점 처리를 통해 자동으로 **다음 주 월요일의 급식과 시간표를 앞당겨 보여줍니다(Smart Forwarding).**

### 2) D-Day 스케줄링 동기화
학급 서버(디스코드 봇 등)와 연계된 수행평가 및 일정 관리를 통해 가장 시급한 일을 파악합니다. 특히 `마감 임박`인 일정은 강렬한 에러 레이어 색상으로 눈에 띄게 강조합니다.

---



# 💻 로컬 환경 설정 및 기여 가이드 (How to Build)

오픈 소스로 함께 기여하기를 원하신다면, API 키 노출 방지를 위해 반드시 거쳐야 하는 보안 규칙이 있습니다.

### 1. 나이스(NEIS) API Key 설정 (`local.properties`)
1. 프로젝트 루트 경로에 `local.properties` 파일을 생성합니다.
2. 발급받은 API 키를 다음 형태로 구성합니다:
   ```properties
   DATA_GSM_API_KEY=사용자의API키_따옴표없이_입력
   ```



# 📱 물리 기기 설치 가이드 (Sideloading)

스토어 정식 배포 앱이 아니므로 PC 명령줄(또는 스마트폰)을 이용해 다운로드 가능합니다.

### 워치 사전 준비 (ADB 무선 디버깅)
1. **[설정] > [워치 정보] > [소프트웨어 정보] > 소프트웨어 버전**을 7번 이상 누르면 '개발자 옵션'이 켜집니다.
2. 개발자 옵션에 들어가 **ADB 디버깅**, **무선 디버깅**을 활성화시킵니다.
3. [무선 디버깅]에 진입해 [+ 새 기기 페어링]을 선택하여 나타난 `IP, 포트번호, 페어링 코드`를 기록합니다.

### PC 연결 및 설치
1. [SDK 플랫폼 도구](https://developer.android.com/tools/releases/platform-tools?hl=ko)를 다운받고 압축 해제한 곳에서 `cmd`(명령 프롬프트) 창을 엽니다.
2. 아래 명령어로 워치를 페어링합니다.
   ```sh
   .\adb.exe pair [위치IP:포트]
   ```
3. 확인된 기기(`.\adb.exe devices`)로 앱을 전송하고 설치합니다.
   ```sh
   .\adb.exe install 파일명.apk
   ```

---

<p align="center">
  <b>GSMwatch</b> Copyright © 2026. <br>
  Released under the MIT License.
</p>
