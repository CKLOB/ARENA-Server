# Auth Implementation Progress

## 전체 목표
Notion Auth 명세에 맞는 OAuth 로그인, 온보딩, purpose 분리 JWT, 기기별 refresh session을 구현하고 검증한다.

## 저장소 분석
- Kotlin 1.9.25, Spring Boot 3.5.16, Java 21, Gradle 8.14.5를 사용한다.
- JPA, Redis, Spring Security, JJWT, springdoc와 Kotest/MockK 테스트 의존성이 있다.
- 현재 `User`에 OAuth identity와 온보딩 필수 필드가 있고, access JWT와 공통 보안 통합 테스트가 있다.
- OAuth provider client, refresh session, Redis state/PKCE, 인증 API와 인증 OpenAPI 계약은 없다.
- Gradle wrapper 파일은 실행 권한이 없어 검증 스크립트는 `bash ./gradlew test`를 사용한다.

## 완료된 작업
- 루프 환경 문서와 검증 스크립트 생성. 검증 결과는 아래에 기록한다.
- JWT purpose claim 분리: `JwtTokenProviderTest`와 `./scripts/verify.sh` 성공.
- access-only 인증 필터: refresh JWT의 보호 API 접근 거부 통합 테스트와 `./scripts/verify.sh` 성공.
- RefreshSession 영속 모델: hash, 만료, 폐기 상태 저장 테스트와 `./scripts/verify.sh` 성공.
- refresh JWT session ID: `sid` claim 단위 테스트와 `./scripts/verify.sh` 성공.
- refresh token hash 및 session 조회: `RefreshTokenHasherTest`, `RefreshSessionRepositoryTest`, `./scripts/verify.sh` 성공.
- refresh token 회전: 이전 session 폐기, 새 token/session 발급, 폐기 token 재사용 거부 테스트와 `./scripts/verify.sh` 성공.
- `/auth/refresh` 필터 분리: 유효 형식이지만 저장되지 않은 refresh token의 공통 401 테스트와 `./scripts/verify.sh` 성공.
- `/auth/refresh` 성공: 저장 session 회전, token pair 200 응답, 이전 session 폐기 테스트와 `./scripts/verify.sh` 성공.
- `/auth/logout`: 현재 기기 session만 폐기하고 204를 반환하며 다른 session을 유지하는 테스트와 `./scripts/verify.sh` 성공.
- 온보딩 완료: 신규 Apple identity로 user와 refresh session을 만들고 access/refresh JWT를 발급하는 `OAuthLoginServiceTest`와 `./scripts/verify.sh` 성공.
- JWT purpose 오용: access, refresh, onboarding JWT의 모든 불일치 expected purpose 조합을 거부하는 `JwtTokenProviderTest`와 `./scripts/verify.sh` 성공.
- OAuth provider mock: Google, Kakao, Apple의 authorization code 교환과 Apple Android state 전달을 검증하는 `OAuthLoginServiceTest`와 `./scripts/verify.sh` 성공.
- OAuth 오류 계약: 잘못된 authorization code의 401과 미지원 provider의 403 공통 응답을 검증하는 `SecurityIntegrationTest`와 `./scripts/verify.sh` 성공.
- OAuth 환경 설정: provider별 client 설정과 WEB/IOS/ANDROID redirect URI 바인딩을 검증하는 `OAuthPropertiesTest`와 `./scripts/verify.sh` 성공.
- Google provider client: authorization code token 교환 요청과 userinfo profile 변환을 mock HTTP로 검증하는 `GoogleOAuthProviderClientTest`와 `./scripts/verify.sh` 성공.
- Kakao provider client: authorization code token 교환 요청과 중첩 user profile 변환을 mock HTTP로 검증하는 `KakaoOAuthProviderClientTest`와 `./scripts/verify.sh` 성공.
- Apple state/PKCE: Redis `getAndDelete`로 verifier를 일회 소비하고, 5분 TTL과 S256 challenge를 검증하는 `AppleOAuthStateServiceTest`, `AppleOAuthAuthorizationServiceTest`, `./scripts/verify.sh` 성공.
- Apple authorize endpoint: Android 요청의 302 Apple Location과 비대상 provider의 403을 검증하는 `SecurityIntegrationTest`와 `./scripts/verify.sh` 성공.
- Apple provider client: Android state verifier를 소비하고 client secret/JWKS를 사용해 Apple `id_token`의 서명·issuer·audience·만료를 검증하는 `AppleOAuthProviderClientTest`와 `./scripts/verify.sh` 성공.
- OAuth login HTTP: 기존 사용자의 access/refresh token과 신규 사용자의 onboarding token 및 `isNewUser` 응답 스키마를 검증하는 `SecurityIntegrationTest`와 `./scripts/verify.sh` 성공.
- 온보딩 접근 제어: onboarding JWT가 보호 API에서 401 `INVALID_TOKEN`으로 거부되는 `SecurityIntegrationTest`와 `./scripts/verify.sh` 성공.

## 현재 작업
- Loop 20: 온보딩 token의 보호 API 접근 거부 완료
- 선택 이유: onboarding token 발급과 purpose 분리는 검증됐지만, 신규 사용자가 온보딩 전 보호 API에 접근할 수 없다는 최종 경계를 HTTP로 직접 증명하지 못했다.
- 검증: onboarding JWT를 Authorization에 넣은 보호 API 요청이 401 `INVALID_TOKEN`을 반환하는 `SecurityIntegrationTest`와 `./scripts/verify.sh` 성공.

## 다음 작업
- 반복 제한 도달 후 미완료 인증 계약과 Notion 갱신 상태를 정리한다.

## 검증 결과
- `./scripts/verify.sh`: exit code 0, `bash ./gradlew test` 성공 (2026-07-21).

## 실패 기록
### TASK2 Attempt 1
- 작업: OpenAPI 인증 계약 schema 테스트
- 실행한 명령: `bash ./gradlew test --tests team.cklob.arena.global.security.SecurityIntegrationTest`
- 실패한 테스트 또는 단계: `/v3/api-docs` JSONPath assertion
- 최초 오류: `PathNotFoundException`
- 추정이 아닌 확인된 원인: OpenAPI는 authorize path를 Apple literal이 아닌 controller template `/auth/{provider}/authorize`로 생성했다.
- 변경한 내용: authorize OpenAPI assertion을 controller template path로 수정했다.
- 재검증 결과: `SecurityIntegrationTest`와 `./scripts/verify.sh` 성공.
- 다음 접근: assertion을 controller template path로 수정하고 DTO schema assertions를 재검증한다.

### TASK2 Attempt 2
- 작업: TASK2 최종 전체 검증
- 실행한 명령: `./scripts/verify.sh`
- 실패한 테스트 또는 단계: Gradle wrapper 시작
- 최초 오류: `gradle-8.14.5-bin.zip.lck (Operation not permitted)`
- 추정이 아닌 확인된 원인: 샌드박스가 `~/.gradle/wrapper/dists` lock 파일 쓰기를 허용하지 않았다.
- 변경한 내용: 없음
- 재검증 결과: Gradle 사용자 캐시 접근 권한으로 `./scripts/verify.sh`를 재실행해 성공했다.
- 다음 접근: 없음

### Attempt 7
- 작업: Apple Android authorize endpoint 통합 테스트
- 실행한 명령: `bash ./gradlew test --tests team.cklob.arena.global.security.SecurityIntegrationTest`
- 실패한 테스트 또는 단계: `SecurityIntegrationTest` Spring context 생성
- 최초 오류: `MockKException` on `StringRedisTemplate.setBeanClassLoader`
- 추정이 아닌 확인된 원인: Spring bean lifecycle이 호출한 `BeanClassLoaderAware` 메서드를 strict `StringRedisTemplate` mock이 처리하지 못했다.
- 변경한 내용: 대기
- 재검증 결과: `SecurityIntegrationTest`와 `./scripts/verify.sh` 성공.
- 다음 접근: lifecycle 메서드를 기본 처리할 수 있도록 해당 mock만 relaxed로 만든다.

### Attempt 6
- 작업: Google OAuth provider client 추가 후 전체 회귀 검증
- 실행한 명령: `./scripts/verify.sh`
- 실패한 테스트 또는 단계: `SecurityIntegrationTest` Spring context 생성
- 최초 오류: `BeanDefinitionOverrideException`
- 추정이 아닌 확인된 원인: 테스트 전용 `googleOAuthProviderClient` bean과 새 `GoogleOAuthProviderClient` component의 기본 bean 이름이 동일하다.
- 변경한 내용: 대기
- 재검증 결과: `SecurityIntegrationTest`와 `./scripts/verify.sh` 성공.
- 다음 접근: 통합 테스트 context에서만 bean override를 허용하여 fake를 유지한다.

### Attempt 5
- 작업: logout 기기별 session 통합 테스트
- 실행한 명령: `bash ./gradlew test --tests team.cklob.arena.global.security.SecurityIntegrationTest`
- 실패한 테스트 또는 단계: test fixture 저장
- 최초 오류: H2 `DataIntegrityViolationException`
- 추정이 아닌 확인된 원인: 다른 기기 session의 fixture hash가 80자여서 `token_hash` 64자 컬럼 제한을 넘었다.
- 변경한 내용: fixture hash를 64자로 맞춘다.
- 재검증 결과: 대기
- 다음 접근: 같은 통합 테스트를 다시 실행한다.

### Attempt 4
- 작업: refresh token 회전 서비스 테스트
- 실행한 명령: `bash ./gradlew test --tests team.cklob.arena.domain.user.application.RefreshTokenRotationServiceTest`
- 실패한 테스트 또는 단계: `compileTestKotlin`
- 최초 오류: `Operator '!=' cannot be applied to 'LocalDateTime?' and 'Boolean?'`
- 추정이 아닌 확인된 원인: Kotest infix assertion의 우선순위 때문에 폐기 여부 비교가 Boolean expression으로 묶이지 않았다.
- 변경한 내용: 폐기 여부 비교를 괄호로 감싼다.
- 재검증 결과: 대기
- 다음 접근: 같은 서비스 테스트를 다시 실행한다.

### Attempt 2
- 작업: JWT purpose claim 분리 단위 테스트
- 실행한 명령: `bash ./gradlew test --tests team.cklob.arena.global.security.JwtTokenProviderTest`
- 실패한 테스트 또는 단계: `compileKotlin`
- 최초 오류: `Conflicting overloads: getUserId(token: String)`
- 추정이 아닌 확인된 원인: 기존 `getUserId(token: String)`가 새 access 기본 메서드와 함께 남아 동일 시그니처가 두 번 선언됐다.
- 변경한 내용: 중복 선언을 제거한다.
- 재검증 결과: 대기
- 다음 접근: 같은 단위 테스트를 다시 실행한다.

### Attempt 3
- 작업: JWT purpose claim 분리 단위 테스트 재실행
- 실행한 명령: `bash ./gradlew test --tests team.cklob.arena.global.security.JwtTokenProviderTest`
- 실패한 테스트 또는 단계: `compileTestKotlin`
- 최초 오류: `Unresolved reference: throwables`와 `shouldThrow`
- 추정이 아닌 확인된 원인: 현재 의존성에는 `kotest-assertions-core`만 있고 `io.kotest.assertions.throwables` assertion 모듈은 없다.
- 변경한 내용: 추가 의존성 대신 `runCatching`과 기존 `shouldBe`로 예외 타입을 검증한다.
- 재검증 결과: 대기
- 다음 접근: 같은 단위 테스트를 다시 실행한다.

### Attempt 1
- 작업: 루프 검증 스크립트 실행
- 실행한 명령: `./scripts/verify.sh`
- 실패한 테스트 또는 단계: Gradle wrapper 시작
- 최초 오류: `gradle-8.14.5-bin.zip.lck (Operation not permitted)`
- 추정이 아닌 확인된 원인: 샌드박스가 `~/.gradle/wrapper/dists`의 lock 파일 쓰기를 허용하지 않았다.
- 변경한 내용: 없음
- 재검증 결과: Gradle 사용자 캐시 접근 권한으로 동일 스크립트를 실행해 성공했다.
- 다음 접근: 없음

## 외부 차단 항목
- OAuth 제공자 콘솔의 client ID/secret, Apple key, redirect URI, Android SHA-1 등록이 필요하다.
- Notion `/auth` 계약 갱신은 연결 권한이 확인된 뒤 수행한다.

## 결정 기록
- provider/platform: Google, Kakao, Apple과 WEB, IOS, ANDROID를 명시적으로 구분한다.
- JWT purpose: access, refresh, onboarding은 서로 다른 purpose claim으로 검증한다. claim 이름과 값은 구현 시 기존 JWT API와 함께 확정한다.
- RefreshSession: 기기별 session을 만들고 refresh token 원문 대신 해시를 저장한다.
- Apple state/PKCE: state와 verifier는 Redis key에 TTL과 함께 보관한다. key 형식은 기존 Redis 사용 패턴을 조사한 뒤 정한다.
- 온보딩 token: 신규 OAuth identity의 가입 완료에만 사용하고 보호 API 인증에는 사용하지 않는다.
- access filter: refresh와 logout은 access 인증 필터에서 분리한다.
- 기존 API: Notion 계약을 갱신하고 springdoc annotation과 같은 계약을 유지한다.
- provider test: 외부 호출 대신 provider client mock 또는 fake를 사용한다.
- JWT purpose 구현: claim 이름은 `purpose`, 값은 `access`, `refresh`, `onboarding`으로 확정했다. `JwtTokenProvider`가 예상 purpose와 일치하지 않으면 JWT를 거부한다.
- RefreshSession 모델: `user_id`, `token_hash`, `expires_at`, `revoked_at`, `created_at`만 저장하며 원문 token 열은 만들지 않는다.
- refresh JWT session claim: refresh purpose token에만 `sid` Long claim을 넣고 access/onboarding token에서는 읽을 수 없게 한다.

## 최종 보고서
- `LOOP.md`의 최대 20회 반복 제한에 도달했다. 전체 검증은 성공했지만 아래 미완료 항목 때문에 Auth 작업은 완료 선언하지 않는다.
- 남은 구현/검증: onboarding endpoint HTTP 상태·요청 스키마, refresh 14일 만료 claim 검증, OAuth 설정에 실제 secret/redirect URI가 없음을 확인하는 보안 점검, OpenAPI 요청/응답 schema annotation 보강, Notion `/auth` 하위 페이지 계약 갱신.
- 외부 운영 작업: Google/Kakao/Apple OAuth 콘솔 설정, Apple private key와 platform redirect URI 배포, Android SHA-1 등록.
- TASK2 완료: 온보딩 HTTP 계약, refresh 14일 claim/configuration, OpenAPI 상태 코드와 DTO schema, test bean override 제거, secret/debug 정적 점검을 완료했다. `./scripts/verify.sh` 성공.
- Notion 갱신: [OAuth 로그인](https://app.notion.com/p/396540bf290481768053dd0c0e53eaed), [토큰 재발급](https://app.notion.com/p/396540bf29048189badafd0a1362826b), [로그아웃](https://app.notion.com/p/396540bf29048141ac49d46e649feb13)를 갱신하고 [Apple Android 인가](https://app.notion.com/p/3a4540bf2904811da0a0faebc299baec), [신규 사용자 온보딩](https://app.notion.com/p/3a4540bf29048124ab28e91b669d49f1)을 추가했다.
- 환경 변수 준비: `.env.example`에 Auth 키를 추가하고 Apple PEM의 escaped `\n` dotenv 값을 처리하도록 보완했다. `AppleOAuthProviderClientTest`와 `./scripts/verify.sh` 성공. 실제 `.env`은 사용자 staged 변경이라 읽거나 수정하지 않았다.
