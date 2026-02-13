# koreanit-server-spring-example

동촌성당 프론트 정적 파일(`src/main/resources/static`)을 함께 서빙하는 **Spring Boot 3 API 서버**입니다.

## 기술 스택
- Java 17
- Spring Boot 3.5.x
- Spring Web / JDBC / Validation / Security
- Spring Session + Redis
- MySQL
- Gradle

## 주요 기능
- 사용자
  - 회원가입: `POST /api/users`
  - 로그인(세션 생성): `POST /api/login`
  - 로그아웃: `POST /api/logout`
  - 내 정보 조회: `GET /api/me`
- 게시글
  - 생성/조회/수정/삭제: `/api/posts`
- 댓글
  - 생성/조회/삭제: `/api/posts/{postId}/comments`, `/api/comments/{id}`
- 정적 리소스
  - `src/main/resources/static` 경로의 파일 제공 (동촌성당 웹 리소스 포함)

## 프로젝트 구조
- `src/main/java/com/koreanit/spring/user` : 유저 도메인
- `src/main/java/com/koreanit/spring/post` : 게시글 도메인
- `src/main/java/com/koreanit/spring/comment` : 댓글 도메인
- `src/main/java/com/koreanit/spring/security` : 인증/세션/보안 설정
- `src/main/resources/application*.yaml` : 환경설정(dev/prod)
- `.api-http/*.http` : API 테스트 예시

## 실행 방법
### 1) 로컬(dev)
`application.yaml`에서 기본 프로필은 `dev`입니다.

필수 실행 환경:
- MySQL
- Redis

실행:
```bash
./gradlew bootRun
```

### 2) 운영(prod)
`application-prod.yaml` 기준 환경변수 필요:
- `PORT`
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`

예:
```bash
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

## 빌드
```bash
./gradlew clean build
```
생성물:
- `build/libs/spring-0.0.1-SNAPSHOT.jar`

## API 테스트
아래 파일을 IntelliJ HTTP Client/VSCode REST Client에서 실행하면 빠르게 확인할 수 있습니다.
- `.api-http/users.http`
- `.api-http/post.http`

## 참고
- 세션 저장소는 Redis 기반(`spring.session.store-type=redis`)입니다.
- 인증/인가 정책은 `security` 패키지의 설정 클래스에서 관리합니다.
  
