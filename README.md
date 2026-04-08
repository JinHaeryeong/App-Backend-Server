# AI Care Hub

<img width="1394" height="478" alt="logo" src="https://github.com/user-attachments/assets/d0caf976-b5a2-449c-9c65-ecfdd9d01c93" />
다솜마을은 요양사와 보호자를 위한 지능형 어르신 관리 시스템입니다. AI 기반 건강 데이터 분석과 실시간 모니터링을 통해 어르신의 건강 상태를 체계적으로 관리할 수 있습니다.

## 🏥 프로젝트 개요

### 주요 기능

-   **어르신 관리**: 어르신 등록, 정보 수정, 상태 모니터링
-   **실시간 건강 데이터**: 심박수, 산소포화도, 호흡수, 걸음수 등 생체 신호 추적
-   **AI 기반 분석**: 1일 건강 데이터를 분석하여 종합 건강 리포트 생성
-   **보호자 연동**: 어르신와 보호자 간의 연결 및 관리
-   **PDF 리포트**: AI 분석 결과를 PDF 형태로 내보내기
-   **반응형 대시보드**: 직관적인 차트와 통계 정보 제공

### 사용자 유형

-   **요양사(Admin)**: 어르신 목록 관리, 상세 데이터 확인, AI 리포트 조회
-   **보호자(Guardian)**: 어르신 정보 조회, 건강 상태 모니터링, AI 리포트 조회

## 🛠 기술 스택

### Frontend
:WEB
-   **React**
-   **Axios**
-   **Vite**
-   **Recharts**

:APP
- **Kotlin**
- **Health Connect API**
- 
### Backend
-   **Spring boot**
-   **MYSQL**
-   **MyBatis**
-   **JPA**



## 📁 프로젝트 구조

```
📦 
├─ .gitattributes
├─ .gitignore
├─ README.md
├─ build.gradle
├─ gradle
│  └─ wrapper
│     ├─ gradle-wrapper.jar
│     └─ gradle-wrapper.properties
├─ gradlew
├─ gradlew.bat
├─ settings.gradle
├─ src
│  ├─ main
│  │  ├─ java
│  │  │  └─ com
│  │  │     └─ dasom
│  │  │        └─ dasomServer
│  │  │           ├─ DasomServerApplication.java
│  │  │           ├─ caregiver
│  │  │           │  ├─ application
│  │  │           │  │  └─ CaregiverService.java
│  │  │           │  ├─ domain
│  │  │           │  │  ├─ Caregiver.java
│  │  │           │  │  └─ CaregiverRepository.java
│  │  │           │  ├─ infrastructure
│  │  │           │  │  ├─ CaregiverJpaRepository.java
│  │  │           │  │  └─ CaregiverRepositoryImpl.java
│  │  │           │  └─ presentation
│  │  │           │     ├─ CaregiverController.java
│  │  │           │     └─ dto
│  │  │           │        └─ CaregiverResponse.java
│  │  │           ├─ domain
│  │  │           │  └─ health
│  │  │           │     ├─ controller
│  │  │           │     │  └─ HealthController.java
│  │  │           │     ├─ dto
│  │  │           │     │  ├─ DailyHealthLogRequest.java
│  │  │           │     │  ├─ HealthRequest.java
│  │  │           │     │  └─ UserHealthResponse.java
│  │  │           │     ├─ entity
│  │  │           │     │  ├─ DailyHealthLog.java
│  │  │           │     │  ├─ HealthLog.java
│  │  │           │     │  ├─ HealthResultLog.java
│  │  │           │     │  └─ HealthStatus.java
│  │  │           │     ├─ repository
│  │  │           │     │  ├─ DailyHealthLogRepository.java
│  │  │           │     │  ├─ HealthLogRepository.java
│  │  │           │     │  └─ HealthResultLogRepository.java
│  │  │           │     └─ service
│  │  │           │        ├─ HealthService.java
│  │  │           │        └─ RhrCalculationService.java
│  │  │           ├─ guardian
│  │  │           │  ├─ application
│  │  │           │  │  └─ GuardianService.java
│  │  │           │  ├─ domain
│  │  │           │  │  ├─ Guardian.java
│  │  │           │  │  └─ GuardianRepository.java
│  │  │           │  ├─ infrastructure
│  │  │           │  │  ├─ GuardianJpaRepository.java
│  │  │           │  │  └─ GuardianRepositoryImpl.java
│  │  │           │  └─ presentation
│  │  │           │     ├─ GuardianController.java
│  │  │           │     └─ dto
│  │  │           │        └─ GuardianResponse.java
│  │  │           ├─ infra
│  │  │           │  ├─ ai
│  │  │           │  │  ├─ LstmAnalysisConsumer.java
│  │  │           │  │  ├─ LstmInferenceService.java
│  │  │           │  │  └─ LstmInputScaler.java
│  │  │           │  └─ storage
│  │  │           │     └─ ImageService.java
│  │  │           ├─ shared
│  │  │           │  ├─ common
│  │  │           │  │  └─ ApiResponse.java
│  │  │           │  ├─ config
│  │  │           │  │  ├─ RabbitMqConfig.java
│  │  │           │  │  ├─ SecurityConfig.java
│  │  │           │  │  └─ WebConfig.java
│  │  │           │  ├─ domain
│  │  │           │  │  ├─ BaseImage.java
│  │  │           │  │  └─ BaseTimeEntity.java
│  │  │           │  ├─ error
│  │  │           │  │  ├─ GlobalExceptionHandler.java
│  │  │           │  │  └─ exception
│  │  │           │  │     └─ UserNotFoundException.java
│  │  │           │  └─ security
│  │  │           │     ├─ JwtAuthenticationFilter.java
│  │  │           │     ├─ JwtTokenProvider.java
│  │  │           │     └─ TokenDto.java
│  │  │           └─ silver
│  │  │              ├─ application
│  │  │              │  ├─ UserDetailService.java
│  │  │              │  └─ UserService.java
│  │  │              ├─ domain
│  │  │              │  ├─ RefreshToken.java
│  │  │              │  └─ Silver.java
│  │  │              ├─ infrastructure
│  │  │              │  ├─ RefreshTokenRepository.java
│  │  │              │  └─ SilverRepository.java
│  │  │              └─ presentation
│  │  │                 ├─ SilverController.java
│  │  │                 └─ dto
│  │  │                    ├─ LoginRequest.java
│  │  │                    ├─ LoginResponse.java
│  │  │                    ├─ SignupRequest.java
│  │  │                    ├─ SignupResponse.java
│  │  │                    └─ SilverResponse.java
│  │  └─ resources
│  │     ├─ application.yml
│  │     └─ model
│  │        └─ lstm_personalized_model_final_v2.onnx
│  └─ test
│     └─ java
│        └─ com
│           └─ dasom
│              └─ dasomServer
│                 └─ DasomServerApplicationTests.java
└─ uploads
   ├─ 37e35d05-2f73-43f0-8379-0d70ef309fc2.jpg
   ├─ 70c4f540-eb0a-4a4f-a8ec-f5b7ecaedbaf.jpg
   ├─ 78c98568-651b-4395-a09e-69c1b043943d.jpg
   └─ defaultProfileImage.png
```
©generated by [Project Tree Generator](https://woochanleee.github.io/project-tree-generator)
## 🚀 시작하기

### 필수 요구사항

-   Node.js (v16 이상)
-   npm 또는 yarn
-   Spring Boot 3.5.6
-   java 21 이상

### 설치 및 실행

1. **저장소 클론**

```bash
git clone [repository-url]
```

2. **의존성 설치**

```bash
npm install
Kotlin 또는 Spring Boot의 경우 build.gradle 파일을 열고 의존성 설치
```

3. **개발 서버 실행**

```bash
npm run dev
```

4. **빌드**

```bash
npm run build
```

## 🔧 주요 기능 상세

### 1. 어르신 관리 시스템

-   **어르신 목록**: 카드 형태로 어르신 정보 표시
-   **상태 분류**: 정상, 주의, 위험 상태로 어르신 분류
-   **검색**: 어르신 검색 기능
-   **어르신 등록**: 새로운 어르신 정보 등록

### 2. 건강 모니터링
-   **생체 신호 추적**: 체중, 혈압(이완기/수축기), 체온, 분당심박수, 혈중 산소 농도, 혈당, 수면 점수, 걸음수, 소모 칼로리
-   **시간대별 분석**: 시간, 일간, 주간, 월간 평균 데이터 제공
-   **상태 알림**: 비정상 수치 감지 시 위험 알림

### 3. AI 기반 건강 리포트

-   **7일간 데이터 분석 및 평가**: 하루 건강 데이터 종합 분석 및 평가
-   **AI 예측**: 정상, 주의, 위험 단계로 건강 상태 분류
-   **시각화**: 차트를 통한 데이터 추이 시각화
-   **PDF 내보내기**: 분석 결과를 PDF 형태로 이메일 전송

### 4. 보호자 연동

-   **보호자-어르신 연결**: 어르신와 보호자 간의 관계 설정
-   **보호자 목록**: 어르신별 보호자 정보 관리
-   **보호자 등록**: 새로운 보호자 정보 등록

## 🔐 인증 시스템

### 로그인 방식

-   **어르신 및 보호자 로그인**: 일반 사용자 인증(보호자 아이디와 동일)
-   **요양사 로그인**: 관리자 권한 인증
-   **비밀번호 정규식 검증**
-   **자동 로그인**
-   **아이디/비밀번호 찾기**

## 📊 데이터 시각화

### 차트 라이브러리

-   **Recharts**: 반응형 차트 컴포넌트
-   **ComposedChart**: 막대 차트와 선 차트 조합
-   **ResponsiveContainer**: 반응형 차트 컨테이너

### 차트 유형

-   **체중**: 일간 체중 변화 추이 및 BMI 지수 계산
-   **혈당**: 식전/식후 혈당 수치 모니터링 및 당뇨 관리
-   **혈압**: 수축기/이완기 혈압 변화 및 고혈압 위험도 분석
-   **혈중 산소 농도**: 시간대별 산소포화도(SpO2) 수치 추적
-   **체온**: 체온 변화 모니터링 및 발열 감지 알림
-   **분당 심박수**: 시간대별 평균 심박수 변화 및 부정맥 감지
-   **활동량**: 걸음수 및 칼로리 소모량 추적 및 운동량 분석
-   **수면 점수**: 수면 시간, 수면 품질, 수면 단계별 분석

### 차트 분석 기능

-   **실시간 모니터링**: 모든 생체 신호의 실시간 추적 및 표시
-   **트렌드 분석**: 장기간 데이터 추이를 통한 건강 상태 변화 파악
-   **임계값 알림**: 정상 범위를 벗어난 수치 감지 시 즉시 알림
-   **비교 분석**: 이전 기간 대비 건강 지표 변화율 계산
-   **예측 모델**: AI 기반 건강 상태 예측 및 위험도 평가
-   **개인화된 기준**: 어르신별 연령, 성별, 병력에 따른 맞춤형 정상 범위 설정

### 데이터 시각화 특징

-   **다중 차트 지원**: 막대 차트, 선 차트
-   **인터랙티브 차트**: 마우스 오버 시 상세 정보 표시
-   **데이터 내보내기**: 차트 데이터를 PDF 형태로 저장
-   **컬러 코딩**: 정상(녹색), 주의(주황), 위험(빨강) 상태별 색상 구분
-   **반응형 차트**: 다양한 화면 크기에 최적화된 차트 표시

## 🎨 UI/UX 특징

### 디자인 시스템

-   **직관적 네비게이션**: 사용자 친화적 인터페이스
-   **상태별 색상 구분**: 정상(녹색), 주의(주황), 위험(빨강)

### 컴포넌트

-   **재사용 가능한 UI**: 모듈화된 컴포넌트 구조
-   **일관된 스타일링**: CSS 모듈을 통한 스타일 관리
-   **아이콘 시스템**: React Icons를 활용한 직관적 아이콘

## 🔌 API 연동

### 백엔드 연동

-   **API Base URL**: `http://localhost:8080/api`
-   **Axios 인스턴스**: 통일된 HTTP 클라이언트 설정
-   **어르신 API**: 어르신 데이터 CRUD 작업
-   **AI 리포트 API**: 건강 데이터 분석 요청

### 빌드 최적화

-   **Vite**: 빠른 빌드 및 HMR
-   **코드 분할**: 효율적인 번들 크기 관리

### 결과


https://github.com/user-attachments/assets/ba1eb3aa-5de3-4de6-badd-28c0d5a6c822


### 환경 설정

-   **개발 환경**: `npm run dev`
-   **프로덕션 빌드**: `npm run build`
-   **프리뷰**: `npm run preview`

**AI Care Hub Frontend** - 지능형 어르신 관리 시스템으로 더 나은 요양 서비스를 제공합니다.

-   **어르신 목록 페이지**
<img width="465" height="222" alt="image" src="https://github.com/user-attachments/assets/4c744ec8-2e1b-498c-9f88-16857989329a" />

-   **어르신 상세 페이지**
  <img width="399" height="252" alt="image" src="https://github.com/user-attachments/assets/874fbf3b-fd26-4595-99fb-94ecff24b269" />

-   **AI 레포트 페이지**
<img width="465" height="222" alt="image" src="https://github.com/user-attachments/assets/9876a7bd-4d72-49ac-ad81-0ae269240dbf" />

