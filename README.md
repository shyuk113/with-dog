# 🐕 WithDog

GPS 기반 산책 기록과 AI 품종별 코스 추천, 산책 메이트 매칭을 결합한 반려견 산책 플랫폼입니다.

> "우리 개에게 맞는 산책을, 함께 걸을 사람과"

## 주요 기능

### 🗺️ GPS 산책 기록
- 실시간 GPS 경로 추적 및 지도 표시
- 이동 거리, 소요 시간, 페이스 자동 계산
- 산책 중 이벤트 태깅(배변, 급수, 휴식 등)
- 반려견별 산책 히스토리 관리

### 🤖 AI 품종별 코스 추천
- 반려견 품종/나이/체중 기반 맞춤 코스 제안
- 날씨, 시간대, 개인 산책 이력을 고려한 추천
- 사용자 피드백을 통한 추천 정확도 개선

### 👥 산책 메이트 매칭 & 커뮤니티
- 위치 기반 산책 메이트 매칭
- 산책 기록 및 코스 후기 공유 피드
- 반려견 프로필 및 채팅 기능

## 기술 스택

| 구분 | 내용 |
|---|---|
| Group | `com.example` |
| Artifact | `withdog` |
| Package | `com.example.withdog` |
| Build | Gradle/Maven (Jar) |
| Language | Java / Kotlin |
| Framework | Spring Boot |

> 프론트엔드, 지도 API, 데이터베이스 등은 추후 확정 예정

## 프로젝트 구조 (예정)

```
withdog/
├── src/main/java/com/example/withdog/
│   ├── walk/          # 산책 기록 도메인
│   ├── recommend/     # AI 코스 추천 도메인
│   ├── community/     # 매칭/커뮤니티 도메인
│   └── user/          # 사용자/반려견 프로필
└── src/main/resources/
```

## 개발 로드맵

- [ ] Phase 1: GPS 산책 기록 + 기본 통계
- [ ] Phase 2: 품종별 규칙 기반 코스 추천
- [ ] Phase 3: 산책 메이트 매칭 / 커뮤니티
- [ ] Phase 4: AI 추천 고도화, 웨어러블 연동

## 라이선스

TBD
