# Auth 후속 작업

## 시작 조건
- `TASK.md`의 Auth 구현과 `./scripts/verify.sh` 성공 상태를 기준으로 한다.
- 실제 OAuth 호출이나 실제 secret을 테스트에 사용하지 않는다.
- 각 작업 전후로 `git status --short`, 관련 diff, 직접 테스트를 확인한다.

## 진행 기록
- 현재 작업: 온보딩 HTTP 계약 및 refresh JWT 14일 만료 claim 검증.
- 선택 이유: 이미 구현된 endpoint/configuration의 계약을 먼저 테스트로 고정하면 이후 OpenAPI와 테스트 구성 정리를 안전하게 진행할 수 있다.
- 예상 검증: `SecurityIntegrationTest`, `JwtTokenProviderTest`, properties binding test 성공 후 `./scripts/verify.sh` 성공.

## 1. 온보딩 HTTP 계약 검증
- [x] `POST /auth/onboarding`의 성공·필수 입력 오류 응답을 MockMvc로 검증했다. (`SecurityIntegrationTest`, `./scripts/verify.sh`, 2026-07-21)

방법:
1. onboarding token으로 `nickname`, `investmentExperience` 요청을 보낸다.
2. HTTP 200과 `accessToken`, `refreshToken` 응답 필드를 확인한다.
3. 생성된 `User`의 provider/provider user ID와 `RefreshSession`의 token hash를 확인한다.
4. 빈 nickname, 누락 token, 잘못된 investmentExperience는 400 공통 오류 응답을 확인한다.

완료 증거:
- `SecurityIntegrationTest` 또는 전용 controller test 성공.
- `./scripts/verify.sh` exit code 0.

## 2. Refresh JWT 14일 만료 검증
- [x] refresh JWT의 `exp`가 발급 시점 기준 14일이며 설정 바인딩으로 변경 가능한지 검증했다. (`JwtTokenProviderTest`, `JwtPropertiesTest`, `./scripts/verify.sh`, 2026-07-21)

방법:
1. `JwtTokenProvider.createRefreshToken` 결과의 JWT payload를 파싱한다.
2. `iat`와 `exp` 차이가 `Duration.ofDays(14)` 범위인지 확인한다.
3. 환경 변수 `JWT_REFRESH_TOKEN_EXPIRATION`으로 기간을 바꾼 configuration binding 테스트도 추가한다.

완료 증거:
- `JwtTokenProviderTest`와 `OAuthPropertiesTest` 또는 별도 properties test 성공.
- `./scripts/verify.sh` exit code 0.

## 3. OpenAPI 인증 계약 보강
- [x] authorize, login, onboarding, refresh, logout의 요청 DTO와 상태 코드 문서를 보강했다. (`SecurityIntegrationTest`, `./scripts/verify.sh`, 2026-07-21)

방법:
1. `AuthController`에 `@Operation`, `@ApiResponse`를 추가한다.
2. request/response DTO 필드에 `@Schema` 예시와 설명을 추가한다.
3. `/v3/api-docs` 테스트에서 모든 auth path와 200/204/401/403 응답 정의를 확인한다.

완료 증거:
- `SecurityIntegrationTest`의 OpenAPI JSONPath assertions 성공.
- Web, iOS, Android가 사용할 `platform`, `state`, token 필드가 OpenAPI에 노출됨.

## 4. 테스트 구성 정리
- [x] `SecurityIntegrationTest`의 bean override 의존을 제거했다. (`SecurityIntegrationTest`, `./scripts/verify.sh`, 2026-07-21)

방법:
1. 실제 `GoogleOAuthProviderClient`와 이름이 겹치지 않는 test fake를 만든다.
2. resolver가 test fake를 선택하도록 test configuration을 명시한다.
3. `spring.main.allow-bean-definition-overriding=true`를 삭제한다.
4. Redis는 HTTP test에서 연결하지 않는 fake/state service로 대체하거나 필요한 동작만 명시적으로 stub한다.

완료 증거:
- bean override 설정 없이 `SecurityIntegrationTest` 성공.
- `./scripts/verify.sh` exit code 0.

## 5. Secret 및 임시 코드 점검
- [x] 실제 OAuth secret, Apple private key, redirect URI, debug code가 diff에 없는지 확인했다. (`git diff --check`, credential/debug 검색, 2026-07-21)

방법:
1. `git diff --check`를 실행한다.
2. `rg -n "BEGIN PRIVATE KEY|client_secret|AIza|sk_"`로 코드와 설정을 점검한다.
3. `application.yaml`에는 환경 변수 placeholder만 남기고 실제 값은 배포 환경에서 주입한다.

완료 증거:
- 점검 결과를 `PROGRESS.md`에 기록한다.
- 실제 credential이 추적 파일에 없음.

## 6. Notion Auth 문서 갱신
- [x] Notion `/auth` 하위 페이지를 구현 계약과 일치시켰다. (2026-07-21)

반영할 계약:
- `GET /auth/APPLE/authorize?platform=ANDROID`: Apple authorization URL로 302 이동, state/PKCE 사용.
- `POST /auth/login/{provider}`: `authorizationCode`, `platform`, Apple Android의 `state`.
- 기존 사용자: `accessToken`, `refreshToken`, `isNewUser: false`.
- 신규 사용자: `onboardingToken`, `isNewUser: true`.
- `POST /auth/onboarding`: onboarding token, nickname, investment experience로 가입 완료 후 token pair 발급.
- `POST /auth/refresh`, `POST /auth/logout`: Authorization refresh token 사용.
- 401: invalid authorization code/token, 403: unsupported provider/authorize flow.

완료 증거:
- Notion 변경 링크 또는 접근 불가 원인을 `PROGRESS.md`에 기록한다.

## 7. 운영 OAuth 콘솔 설정
- [ ] 배포 환경에서 OAuth provider 설정을 완료한다.

이 섹션은 저장소 코드만으로 완료할 수 없는 사람 작업이다. 1~6의 코드·검증·문서 작업은 완료됐다.
- [x] `.env.example`에 모든 Auth 환경 변수 키와 형식을 추가했고, Apple private key의 escaped `\n` dotenv 형식을 지원한다. (`AppleOAuthProviderClientTest`, `./scripts/verify.sh`, 2026-07-21)

필요 값:
- Google: client ID/secret, WEB/IOS/ANDROID redirect URI.
- Kakao: REST API key/client secret, WEB/IOS/ANDROID redirect URI.
- Apple: Services ID 또는 앱 client ID, team ID, key ID, private key, Android web redirect URI.
- Android: Google/Kakao 콘솔에 release keystore SHA-1 등록.
- 배포 secret store: `JWT_SECRET`, OAuth secret, Apple private key를 환경 변수로 주입.

완료 증거:
- 운영 환경에서 각 provider sandbox 로그인 smoke test 성공.
- secret 값은 저장소와 Notion에 기록하지 않는다.

## 완료 기준
- [x] 1~6의 코드/문서 작업이 검증 증거와 함께 완료됐다.
- [x] `./scripts/verify.sh`가 성공한다. (2026-07-21)
- [x] OpenAPI와 Notion 계약이 일치한다.
- [ ] 운영 콘솔 작업은 담당자와 완료 상태가 별도로 기록됐다.
