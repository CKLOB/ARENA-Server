# Auth 명세 기반 구현

## 목표
- [ ] 웹, iOS, Android에서 Google, Kakao, Apple OAuth 로그인과 기기별 refresh session을 제공한다.
- [ ] 신규 사용자는 온보딩 완료 전 access/refresh JWT로 보호 API에 접근할 수 없다.

## 현재 상태
- [x] Kotlin 1.9.25, Spring Boot 3.5.16, Java 21, Gradle 8.14.5와 JPA, Redis, Spring Security, JJWT 의존성이 준비되어 있다.
- [x] `User`는 OAuth provider와 provider user ID의 복합 유니크 제약, 닉네임과 투자 경험 필수 필드를 가진다.
- [x] access JWT 발급, JWT 인증 필터, `/auth/**` 공개 경로, 공통 응답과 보안 통합 테스트가 있다.
- [ ] OAuth provider client, 인증 controller/service, refresh session, Redis state/PKCE, OpenAPI 인증 계약은 없다.
- [ ] 기존 JWT에는 purpose claim과 refresh/onboarding 만료 설정이 없다.
- [ ] 현재 Notion `/auth`의 로그인·refresh·logout 계약은 신규 온보딩과 플랫폼 입력을 반영하도록 갱신해야 한다.
- [ ] OAuth 콘솔의 client ID/secret, Apple key, 플랫폼별 redirect URI, Android SHA-1은 외부 설정이 필요하다.

## 구현 범위

### 1. 계약 및 API
- [x] `GET /auth/{provider}/authorize?platform=ANDROID` 계약을 구현한다. (`SecurityIntegrationTest`, `./scripts/verify.sh`, 2026-07-21)
- [ ] `POST /auth/login/{provider}`가 authorization code, platform, 필요한 Apple state를 받도록 구현한다.
- [x] 기존 사용자는 access token, refresh token, `isNewUser: false`를 200 응답으로 받는 테스트를 추가한다. (`SecurityIntegrationTest`, `./scripts/verify.sh`, 2026-07-21)
- [x] 신규 사용자는 onboarding token과 `isNewUser: true`만 받는 테스트를 추가한다. (`SecurityIntegrationTest`, `./scripts/verify.sh`, 2026-07-21)
- [ ] `POST /auth/onboarding`, `POST /auth/refresh`, `POST /auth/logout`의 상태 코드와 응답 스키마를 구현한다.

### 2. OAuth 제공자 연동
- [x] Google authorization code 교환 provider client를 구현한다. (`GoogleOAuthProviderClientTest`, `./scripts/verify.sh`, 2026-07-21)
- [x] Kakao authorization code 교환 provider client를 구현한다. (`KakaoOAuthProviderClientTest`, `./scripts/verify.sh`, 2026-07-21)
- [x] Apple authorization code 교환 provider client를 구현한다. (`AppleOAuthProviderClientTest`, `./scripts/verify.sh`, 2026-07-21)
- [ ] provider와 provider user ID로 기존 사용자를 조회하고, 이메일 기반 자동 연결을 하지 않는다.
- [x] Google, Kakao, Apple mock provider client의 코드 교환 테스트를 추가한다. (`OAuthLoginServiceTest`, `./scripts/verify.sh`, 2026-07-21)
- [x] 유효하지 않은 authorization code의 401과 지원하지 않는 provider의 403 테스트를 추가한다. (`SecurityIntegrationTest`, `./scripts/verify.sh`, 2026-07-21)

### 3. Apple Android state와 PKCE
- [x] Android Apple authorize URL에 state와 PKCE challenge를 포함한다. (`AppleOAuthAuthorizationServiceTest`, `./scripts/verify.sh`, 2026-07-21)
- [x] verifier와 state를 Redis에 TTL과 함께 저장한다. (`AppleOAuthStateServiceTest`, `./scripts/verify.sh`, 2026-07-21)
- [x] Apple login에서 state를 검증하고 verifier로 code를 교환한다. (`AppleOAuthProviderClientTest`, `./scripts/verify.sh`, 2026-07-21)
- [x] 정상, 불일치, 만료 state/PKCE 테스트를 추가한다. (`AppleOAuthStateServiceTest`, `./scripts/verify.sh`, 2026-07-21)

### 4. 신규 사용자 온보딩
- [ ] 신규 OAuth 식별자에 짧은 만료의 onboarding token을 발급한다.
- [ ] onboarding 요청에서 닉네임과 투자 경험을 검증하고 `User`를 생성한다.
- [x] 온보딩 완료 후 access/refresh JWT 발급 테스트를 추가한다. (`OAuthLoginServiceTest`, `./scripts/verify.sh`, 2026-07-21)
- [x] 온보딩 전 보호 API 접근 거부 테스트를 추가한다. (`SecurityIntegrationTest`, `./scripts/verify.sh`, 2026-07-21)

### 5. JWT 용도 분리
- [x] access, refresh, onboarding JWT에 명시적 purpose claim을 넣는다. (`JwtTokenProviderTest`, 2026-07-21)
- [x] access 인증 필터가 access token만 인증하도록 변경한다. (`SecurityIntegrationTest`, 2026-07-21)
- [x] refresh/logout 경로를 일반 access 인증 필터와 분리한다. (`SecurityIntegrationTest`, 2026-07-21)
- [x] access, refresh, onboarding token 간 용도 오용 거부 테스트를 추가한다. (`JwtTokenProviderTest`, `./scripts/verify.sh`, 2026-07-21)

### 6. RefreshSession과 토큰 회전
- [x] 기기별 refresh session에 token hash, 만료 시각, 폐기 시각을 저장한다. (`RefreshSessionRepositoryTest`, 2026-07-21)
- [x] refresh token 원문을 DB에 저장하지 않는다. (`RefreshSessionRepositoryTest`, 2026-07-21)
- [ ] refresh token 만료를 14일 설정으로 둔다.
- [x] `POST /auth/refresh`가 refresh token으로 새 JWT 쌍을 발급하고 기존 세션을 폐기한다. (`SecurityIntegrationTest`, 2026-07-21)
- [x] refresh 회전, 이전 token 재사용 거부 테스트를 추가한다. (`RefreshTokenRotationServiceTest`, 2026-07-21)

### 7. 로그아웃
- [x] `POST /auth/logout`이 Authorization의 refresh token으로 해당 session만 폐기하고 204를 반환한다. (`SecurityIntegrationTest`, 2026-07-21)
- [x] 기기별 로그아웃과 다른 기기 session 유지 테스트를 추가한다. (`SecurityIntegrationTest`, 2026-07-21)

### 8. 환경 설정
- [x] OAuth client ID, secret, 플랫폼별 redirect URI를 환경 변수 기반 설정으로 추가한다. (`OAuthPropertiesTest`, `./scripts/verify.sh`, 2026-07-21)
- [x] Apple team/key/private key와 JWT 만료 설정을 환경 변수 기반 설정으로 추가한다. (`OAuthPropertiesTest`, `JwtTokenProviderTest`, `./scripts/verify.sh`, 2026-07-21)
- [ ] 실제 secret, private key, redirect URI를 커밋하지 않는다.

### 9. OpenAPI 문서
- [ ] 인증 controller와 DTO에 새 인증 계약의 springdoc annotation을 추가한다.
- [ ] 생성된 `/v3/api-docs`에 인증 경로와 요청/응답이 포함되는 테스트를 추가한다.

### 10. 테스트
- [ ] 외부 OAuth 서버 대신 provider client mock 또는 fake를 사용한다.
- [x] `bash ./gradlew test`가 전체 회귀 테스트를 통과한다. (`./scripts/verify.sh`, 2026-07-21)
- [x] `./scripts/verify.sh`가 exit code 0으로 종료한다. (2026-07-21)

### 11. Notion 문서 갱신
- [ ] Notion `/auth` 하위 페이지를 authorize, login, onboarding, refresh, logout의 새 계약으로 갱신한다.
- [ ] Notion 접근 불가 시 완료로 표시하지 않고 PROGRESS.md에 차단 사유를 기록한다.

## 제외 범위
- [ ] 거래 결정, 추천 생성, 챌린지·주문·포트폴리오 기능은 구현하지 않는다.
- [ ] Flyway 또는 Liquibase는 이번 작업에 도입하지 않는다.
- [ ] OAuth 콘솔과 Android SHA-1 등록은 코드 변경으로 대체하지 않는다.

## 가정
- [ ] 웹, iOS, Android는 토큰 응답 본문을 받고 각 플랫폼의 안전 저장소에 보관한다.
- [ ] JPA DDL auto를 유지한다.
- [ ] OAuth 콘솔 설정과 secret 배포는 사람이 별도로 준비한다.

## 세부 완료 조건
- [ ] 각 TASK 체크는 관련 테스트, HTTP 상태/스키마, DB/Redis 상태, JWT claim, OpenAPI annotation, Notion 변경 중 최소 하나의 검증 증거가 PROGRESS.md에 기록된 뒤에만 완료한다.
- [ ] Redis state/PKCE key는 TTL이 있고, refresh session DB에는 원문 token이 없다.
- [ ] 새 인증 계약의 테스트가 존재하고 기존 보안 통합 테스트가 유지된다.

## 최종 완료 조건
- [ ] 모든 필수 체크박스가 검증 증거와 함께 완료됐다.
- [x] `bash ./gradlew test`와 `./scripts/verify.sh`가 성공한다. (2026-07-21)
- [ ] git diff에 debug code, 임시 OAuth 구현, 실제 secret이 없다.
- [ ] PROGRESS.md에 미해결 실패가 없고 Notion 갱신 또는 명확한 외부 차단이 기록됐다.
