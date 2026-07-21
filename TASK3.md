# Task 3: 운영 OAuth 설정 및 검증

## 목적
- 저장소의 Auth 구현을 실제 Web, iOS, Android 클라이언트와 OAuth 콘솔 설정에 연결한다.
- 실제 secret 값은 Git, Notion, 채팅에 기록하지 않는다.

## 1. 배포 환경 변수 주입
- [ ] 배포 secret store에 `.env.example`의 모든 값을 등록한다.
- [ ] `JWT_SECRET`에 Base64 인코딩된 32바이트 이상 HMAC key를 등록한다.
- [ ] Google/Kakao/Apple의 client ID, client secret, redirect URI를 등록한다.
- [ ] `APPLE_PRIVATE_KEY`는 dotenv에서 literal `\n` 형식으로 등록한다.
- [ ] `.env`는 로컬 전용으로 유지하고 Git에 커밋하지 않는다.

## 2. Google Cloud Console
- [ ] Web, iOS, Android OAuth client를 생성하거나 기존 client를 확인한다.
- [ ] 각 플랫폼의 redirect URI를 `GOOGLE_OAUTH_REDIRECT_URI_WEB`, `IOS`, `ANDROID` 값과 정확히 일치시킨다.
- [ ] Android release keystore SHA-1을 OAuth Android client에 등록한다.
- [ ] Android debug SHA-1이 필요한 개발 환경이면 별도로 등록한다.

## 3. Kakao Developers
- [ ] Web OAuth redirect URI를 `KAKAO_OAUTH_REDIRECT_URI_WEB`과 일치시킨다.
- [ ] Android release keystore SHA-1을 등록한다.
- [ ] Android debug SHA-1이 필요한 개발 환경이면 별도로 등록한다.
- [ ] REST API key/client secret을 `KAKAO_OAUTH_CLIENT_ID`, `KAKAO_OAUTH_CLIENT_SECRET`에 주입한다.
- [ ] iOS/Android SDK는 `authorizationCode` 대신 `accessToken`을 `POST /auth/login/KAKAO`에 전달한다.

## 4. Apple Developer
- [ ] iOS Bundle ID를 `APPLE_OAUTH_IOS_CLIENT_ID`, Android WebView Services ID를 `APPLE_OAUTH_ANDROID_CLIENT_ID`에 주입한다.
- [ ] Sign in with Apple private key를 발급해 `APPLE_PRIVATE_KEY`에 주입한다.
- [ ] Android WebView callback URI를 `APPLE_OAUTH_ANDROID_REDIRECT_URI`와 정확히 일치시킨다.
- [ ] iOS native Sign in with Apple capability와 bundle ID를 확인한다.

## 5. 배포 후 smoke test
- [ ] Web에서 Google 로그인: 기존 사용자 token pair 또는 신규 사용자 onboarding token을 확인한다.
- [ ] iOS에서 Google, Kakao, Apple native 로그인 흐름을 확인한다.
- [ ] Android에서 Google, Kakao native 로그인과 Apple `GET /auth/APPLE/authorize?platform=ANDROID` 302 흐름을 확인한다.
- [ ] 신규 사용자 온보딩 후 access/refresh token을 확인한다.
- [ ] `POST /auth/refresh` 후 이전 refresh token이 거부되는지 확인한다.
- [ ] `POST /auth/logout` 후 해당 refresh token이 거부되는지 확인한다.

## 완료 기준
- [ ] 세 플랫폼의 필요한 provider 로그인 smoke test가 성공한다.
- [ ] 운영 secret이 저장소 추적 파일에 없다.
- [ ] 배포 환경에서 `./scripts/verify.sh`에 대응하는 CI 검증이 성공한다.
