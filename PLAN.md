현재 저장소에 명세 기반 개발 루프를 구축하라.

이번 작업의 목적은 제공된 Auth 명세를 기준으로, 이후 에이전트가 반복적으로 구현·검증·수정할 수 있는 루프 환경을 만드는 것이다.

AGENTS.md는 이미 존재하므로 절대 생성하거나 수정하지 마라. 작업 시작 시 반드시 읽고, 그 규칙을 따른다.

다음 파일만 생성하거나 기존 파일이 있다면 내용을 보존하면서 필요한 항목을 통합하라.

* TASK.md
* LOOP.md
* PROGRESS.md
* scripts/verify.sh

작업 전에 저장소 전체 구조, 빌드 도구, Spring Boot 버전, Java 또는 Kotlin 버전, 인증 관련 코드, JWT 구성, Redis 설정, JPA Entity, 테스트 구조를 먼저 확인하라.

존재하지 않는 라이브러리, 플러그인, 명령어를 임의로 가정하지 마라.

1. TASK.md 작성

아래 Auth 명세를 구현 가능한 작업 명세로 변환하라.

TASK.md에는 반드시 다음 구조를 사용하라.

# Auth 명세 기반 구현
## 목표
## 현재 상태
## 구현 범위
### 1. 계약 및 API
### 2. OAuth 제공자 연동
### 3. Apple Android state와 PKCE
### 4. 신규 사용자 온보딩
### 5. JWT 용도 분리
### 6. RefreshSession과 토큰 회전
### 7. 로그아웃
### 8. 환경 설정
### 9. OpenAPI 문서
### 10. 테스트
### 11. Notion 문서 갱신
## 제외 범위
## 가정
## 세부 완료 조건
## 최종 완료 조건

각 요구사항은 체크박스로 작성하라.

각 완료 조건은 가능한 한 다음 중 하나로 검증 가능하게 작성하라.

* 테스트 클래스 또는 테스트 메서드
* HTTP 상태 코드와 응답 스키마
* DB 저장 상태
* Redis key와 TTL
* JWT claim
* 빌드 또는 테스트 명령
* OpenAPI annotation 존재 여부
* Notion 문서 변경 여부

하나의 요구사항에 구현, 테스트, 문서 변경이 모두 필요하면 각각 별도 체크박스로 나누어라.

명세에 없는 클래스명, 패키지명, DB 컬럼명은 임의로 강제하지 마라. 기존 저장소 구조와 AGENTS.md 규칙을 우선한다.

현재 상태에는 저장소를 조사한 결과를 기록하라.

* 이미 구현된 항목
* 일부 구현된 항목
* 구현되지 않은 항목
* 기존 계약과 새 계약의 충돌
* 확인이 필요한 외부 설정

검증 없이 완료된 것으로 표시하지 마라.

2. LOOP.md 작성

에이전트가 TASK.md 전체를 한 번에 수정하지 않고, 가장 작은 작업 단위로 반복하도록 작성하라.

다음 절차를 반드시 포함하라.

1. AGENTS.md, TASK.md, PROGRESS.md를 읽는다.
2. 현재 git status, git diff, 관련 구현과 테스트를 확인한다.
3. TASK.md에서 아직 완료되지 않은 가장 작은 작업 하나를 선택한다.
4. 선택한 작업, 선택 이유, 예상 검증 방법을 PROGRESS.md에 기록한다.
5. 관련 코드와 테스트를 먼저 조사한다.
6. 필요한 최소 범위만 구현한다.
7. 해당 작업에 직접 관련된 테스트를 먼저 실행한다.
8. 관련 테스트가 성공하면 ./scripts/verify.sh를 실행한다.
9. 실패하면 로그에서 최초 원인을 찾고 PROGRESS.md에 기록한다.
10. 실패 원인을 수정한 뒤 관련 테스트와 전체 검증을 다시 실행한다.
11. 실제 검증 증거가 있을 때만 TASK.md 체크박스를 완료 처리한다.
12. 다음 미완료 작업으로 이동한다.
13. 모든 완료 조건이 충족되면 최종 보고서를 작성하고 종료한다.

다음 실패 방지 규칙을 포함하라.

* 같은 실패가 3회 반복되면 동일한 접근을 중단한다.
* 요구사항을 축소해서 통과시키지 않는다.
* 테스트를 삭제하거나 비활성화하지 않는다.
* 테스트 assertion을 약화해서 통과시키지 않는다.
* OAuth, JWT, Redis 동작을 임시 하드코딩으로 대체하지 않는다.
* Secret, OAuth client secret, Apple private key, JWT secret을 출력하거나 커밋하지 않는다.
* 기존 사용자 변경사항을 함부로 되돌리지 않는다.
* 요구사항과 무관한 대규모 리팩터링을 하지 않는다.
* 전체 검증 실패 상태에서 완료를 선언하지 않는다.
* 외부 OAuth 서버가 필요한 테스트는 provider client mock 또는 fake를 사용한다.
* 실제 OAuth secret이 없다는 이유로 핵심 도메인 테스트를 생략하지 않는다.
* Notion 접근 권한이 없으면 문서 갱신을 완료 처리하지 않고 차단 사유를 기록한다.

다음 반복 제한을 포함하라.

* 전체 루프 최대 반복 횟수: 20회
* 하나의 작업에 대한 동일 실패 최대 반복 횟수: 3회
* 제한에 도달하면 무리하게 계속 수정하지 않는다.
* 현재 상태, 완료된 작업, 실패 원인, 사람의 판단이 필요한 항목을 PROGRESS.md에 기록하고 종료한다.

완료 조건은 다음과 같이 정의하라.

* TASK.md의 모든 필수 체크박스가 검증 증거와 함께 완료됐다.
* 새 인증 계약에 대한 테스트가 존재한다.
* 전체 회귀 테스트가 통과한다.
* ./scripts/verify.sh가 exit code 0으로 종료된다.
* git diff에 디버깅 코드, 임시 코드, 실제 Secret이 없다.
* 미해결 실패가 PROGRESS.md에 남아 있지 않다.
* Notion 문서 갱신이 완료됐거나, 접근 불가가 명확한 외부 차단 항목으로 기록됐다.

3. PROGRESS.md 작성

다음 구조를 사용하라.

# Auth Implementation Progress
## 전체 목표
## 저장소 분석
## 완료된 작업
## 현재 작업
## 다음 작업
## 검증 결과
## 실패 기록
## 외부 차단 항목
## 결정 기록
## 최종 보고서

실패 기록은 각 시도마다 다음 정보를 남길 수 있게 작성하라.

### Attempt N
- 작업:
- 실행한 명령:
- 실패한 테스트 또는 단계:
- 최초 오류:
- 추정이 아닌 확인된 원인:
- 변경한 내용:
- 재검증 결과:
- 다음 접근:

결정 기록에는 다음과 같은 중요한 설계 결정을 기록하게 하라.

* provider와 platform 분기 방식
* JWT purpose claim 이름과 값
* RefreshSession 식별 방식
* refresh token 원문 저장 금지 및 해시 방식
* Apple state와 PKCE Redis key 구조
* 온보딩 토큰에서 허용되는 동작
* access 인증 필터 제외 경로
* 기존 API와의 호환성 처리
* 테스트에서 외부 provider를 대체하는 방식

4. scripts/verify.sh 작성

현재 저장소의 실제 빌드 도구와 테스트 구성을 조사한 뒤 작성하라.

Gradle Wrapper가 존재한다면 기본적으로 다음을 실행한다.

./gradlew test

필요한 경우 저장소에서 실제로 사용할 수 있는 검증 작업만 추가한다.

예:

./gradlew test
./gradlew build

다음 규칙을 따른다.

* set -euo pipefail을 사용한다.
* 존재하지 않는 Gradle task를 임의로 추가하지 않는다.
* clean 때문에 불필요하게 검증 시간이 길어지는 경우 기본 루프에서는 사용하지 않는다.
* 전체 빌드가 별도로 필요하면 실제 task 존재 여부를 확인하고 추가한다.
* 각 단계 시작과 성공 여부를 출력한다.
* 하나라도 실패하면 non-zero exit code로 종료한다.
* 실행 권한을 부여할 수 있도록 파일을 생성한다.

예상 형식:

#!/usr/bin/env bash
set -euo pipefail
echo "==> Running tests"
./gradlew test
echo "==> Running build"
./gradlew build
echo "==> Verification passed"

단, 저장소 구조와 실제 Gradle task를 확인한 결과에 따라 조정하라.

Auth 명세

계약 갱신

* Notion /auth 하위 페이지를 다음 계약으로 갱신한다.
* GET /auth/{provider}/authorize?platform=ANDROID
    * Android Apple 웹 OAuth용 인가 URL을 반환한다.
* POST /auth/login/{provider}
    * authorizationCode
    * platform
    * Apple 웹 OAuth인 경우 state
      를 요청으로 받는다.
* 기존 사용자 응답:
    * accessToken
    * refreshToken
    * isNewUser: false
* 신규 사용자 응답:
    * onboardingToken
    * isNewUser: true
    * 앱 access/refresh JWT는 발급하지 않는다.
* POST /auth/onboarding
    * 온보딩 토큰
    * 닉네임
    * 투자 경험
      을 받아 사용자를 생성한 후 access/refresh JWT 쌍을 반환한다.
* POST /auth/refresh
    * Authorization: Bearer {refreshToken}을 사용한다.
* POST /auth/logout
    * Authorization: Bearer {refreshToken}을 사용한다.
    * 해당 기기의 refresh session만 폐기한다.
    * 204 No Content를 반환한다.
* springdoc OpenAPI annotation을 위 계약과 동일하게 갱신한다.

인증 구현

* Google, Kakao, Apple authorization code를 플랫폼별 OAuth client 설정으로 교환한다.
* 공급자 프로필은 provider + providerUserId로 식별한다.
* 동일 이메일 기반 자동 계정 연결은 하지 않는다.
* Android Apple authorize endpoint는 PKCE verifier와 state를 Redis에 TTL과 함께 저장한다.
* Apple login 요청에서 state를 검증한 후 authorization code를 교환한다.
* 신규 사용자에게 짧은 만료의 용도 제한 온보딩 JWT를 발급한다.
* 온보딩 완료 전에는 보호 API 접근이 불가능해야 한다.
* 닉네임과 투자 경험은 온보딩 요청에서 직접 받아 기존 User 필수 필드를 채운다.
* RefreshSession을 추가한다.
* RefreshSession에는 기기별 refresh token 해시, 만료 시각, 폐기 시각을 저장한다.
* refresh token은 14일 동안 유효하다.
* refresh 재발급 시 기존 세션을 폐기하고 새 토큰으로 회전한다.
* access, refresh, onboarding token에 명시적인 용도 claim을 넣는다.
* refresh 및 logout endpoint는 일반 access 인증 필터와 분리한다.
* access token을 refresh token으로 오용할 수 없어야 한다.
* refresh token을 access token으로 오용할 수 없어야 한다.
* OAuth client ID, secret, 플랫폼별 redirect URI, Apple 키 정보, 토큰 만료값은 환경 변수 기반 설정으로 둔다.

테스트

* provider client mock으로 Google 코드 교환을 검증한다.
* provider client mock으로 Kakao 코드 교환을 검증한다.
* provider client mock으로 Apple 코드 교환을 검증한다.
* 잘못된 authorization code를 검증한다.
* 지원하지 않는 provider를 검증한다.
* 기존 사용자 로그인 시 access/refresh token과 isNewUser: false를 반환하는지 검증한다.
* 신규 사용자 로그인 시 onboarding token만 반환하고 앱 JWT를 발급하지 않는지 검증한다.
* 온보딩 완료 후 access/refresh JWT가 발급되는지 검증한다.
* Android Apple state와 PKCE 정상 케이스를 검증한다.
* Android Apple state 불일치 케이스를 검증한다.
* Android Apple state 만료 케이스를 검증한다.
* refresh token 회전을 검증한다.
* 이전 refresh token 재사용 거부를 검증한다.
* 기기별 로그아웃을 검증한다.
* 로그아웃하지 않은 다른 기기 세션이 유지되는지 검증한다.
* refresh token 14일 만료를 검증한다.
* access, refresh, onboarding token 간 용도 오용이 거부되는지 검증한다.
* 전체 회귀 테스트는 ./gradlew test로 실행한다.

가정

* 웹, iOS, Android 모두 토큰 응답 본문을 수신한다.
* 각 클라이언트는 토큰을 안전 저장소에 보관한다.
* JPA DDL auto를 유지한다.
* 이번 변경에는 Flyway 또는 Liquibase를 도입하지 않는다.
* OAuth 콘솔의 웹, iOS, Android redirect URI 등록은 환경 설정으로 별도 준비한다.
* Android SHA-1 등록과 provider secret 배포는 환경 설정으로 별도 준비한다.

최종 작업 방식

이번 실행에서는 Auth 기능 전체를 구현하지 마라.

이번 실행의 범위는 다음뿐이다.

1. 저장소 조사
2. TASK.md 생성 또는 갱신
3. LOOP.md 생성 또는 갱신
4. PROGRESS.md 생성 또는 갱신
5. scripts/verify.sh 생성 또는 갱신
6. 생성한 파일의 일관성 검토
7. scripts/verify.sh가 실행 가능한지 확인

AGENTS.md는 읽기만 하고 절대 수정하지 마라.

완료 후 다음을 보고하라.

* 생성하거나 수정한 파일
* 저장소 분석 결과
* TASK.md에 반영한 주요 작업 순서
* verify.sh가 실행하는 실제 명령
* 아직 구현하지 않은 사항
* 외부 접근 또는 사람이 준비해야 하는 항목
