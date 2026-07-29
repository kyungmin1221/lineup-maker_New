# lineupMaKer_BE

축구 라인업/포메이션 공유 서비스 **lineupMaker**의 백엔드. 원래는 FE(React) + Firebase(Firestore,
Anonymous Auth)만으로 동작하던 서비스였고, 이 저장소는 Firestore가 담당하던 데이터 저장/조회/인가
로직을 PostgreSQL + Spring Boot + JPA로 옮기기 위한 프로젝트다. **목표는 Firebase(Firestore +
Anonymous Auth)를 완전히 제거하는 것**이며 영구적으로 병행 운영하지 않는다 — 백엔드가 실제로
서비스 가능한 상태가 된 뒤 FE를 이 API로 전환하는 컷오버를 거쳐 Firebase를 걷어낸다 (아래 "Firebase
제거 컷오버 계획" 참고). FE(React)는 UI만 유지하고 `lineupmaker_FE` 저장소(자매 프로젝트, 같은
상위 폴더의 `lineup_new/lineupmaker_FE`)에 남는다.

## 기술 스택

- Java 21 / Spring Boot 4.1.0
- Spring Data JPA + PostgreSQL
- Gradle (wrapper 포함, `./gradlew`)
- Lombok
- Docker / docker-compose (로컬 개발 및 실행 환경)

## 프로젝트 구조

```
src/main/java/org/example/lineupmaker_be/
  LineupMaKerBeApplication.java   # 엔트리포인트
src/main/resources/
  application.properties          # spring.application.name만 정의
  config/application.yml          # datasource/jpa 설정 (classpath:/config/ 는 스프링 기본 탐색 경로)
docs/
  entity-design.md                # JPA 엔티티 설계
  api-spec.md                     # REST API 엔드포인트 / DTO 스펙
Dockerfile                        # 멀티스테이지 빌드 (gradle build → JRE 21 런타임)
docker-compose.yml                # postgres + backend 서비스
```

패키지는 아직 `org.example.lineupmaker_be` 뿐이고 도메인 코드(엔티티/컨트롤러/서비스/리포지토리)는
설계 문서만 있고 구현 전이다. 새 코드를 추가할 때는 `org.example.lineupmaker_be` 하위에
`domain`(엔티티), `repository`, `service`, `web`(컨트롤러/DTO) 정도로 계층을 나누는 것을 기본으로 한다.

## 반드시 읽어야 할 문서

- **`docs/entity-design.md`** — `Lineup`, `LockerRoom` 엔티티 및 jsonb 하이브리드 설계 이유
- **`docs/api-spec.md`** — 엔드포인트 목록, 요청/응답 DTO, 인증 헤더(`X-Device-Id`, `X-Edit-Token`) 규약

코드를 작성하기 전에 위 두 문서와 실제 구현이 어긋나지 않는지 확인할 것. 스펙을 변경하게 되면
문서도 함께 갱신한다.

## 도메인 배경 (왜 이런 구조인가)

- 원래 Firestore의 `lineups`/`lockerRooms` 컬렉션을 그대로 옮기는 것이 목표라, 데이터 모양은
  FE의 `src/constants.js`(`makeQuarter`, `FORMATIONS` 등)와 `src/firebase/lineupService.js`,
  `src/firebase/lockerRoomService.js`의 함수 시그니처를 기준으로 설계했다.
- `quarters`는 쿼터 → 시나리오 → 스텝으로 깊게 중첩되고 자주 구조가 바뀌므로 완전 정규화하지 않고
  PostgreSQL `jsonb` 컬럼에 저장한다 (자세한 이유는 `docs/entity-design.md` 참고).
- 인증은 Firebase Anonymous Auth를 완전히 대체한다. 클라이언트가 생성한 device id를
  `X-Device-Id` 헤더로 보내고 서버가 `owner_id`로 신뢰하는 방식이 컷오버 이후의 영구적인 방식이다.
  더 강한 위변조 방지가 필요해지면 서버 발급 JWT로 교체할 수 있게 인가 로직은 서비스 레이어 한 곳에
  모은다 (이 교체는 Firebase 제거와는 무관한 별도 결정).
- 편집 권한 공유는 Firestore 시절의 `editToken`(랜덤 hex) 방식을 그대로 유지한다.

## Firebase 제거 컷오버 계획

Firestore/Firebase Anonymous Auth와 PostgreSQL/Spring Boot API를 동시에 운영하는 기간을 최소화하기
위해 아래 순서로 진행한다.

1. **BE 개발 및 검증 (현재 단계)** — `docs/entity-design.md`, `docs/api-spec.md` 기준으로 엔티티·
   서비스·컨트롤러 구현. FE는 지금처럼 Firebase를 그대로 사용하며 영향 없음.
2. **FE 전환** — FE의 `src/firebase/lineupService.js`, `src/firebase/lockerRoomService.js`,
   `src/firebase/auth.js` 호출부를 이 BE의 REST API 호출로 교체. `ensureSignedIn()`(Firebase
   Anonymous Auth)은 자체 발급 device id(localStorage 저장)로 대체하고, 실시간 댓글 동기화
   (`onSnapshot`)는 폴링 또는 SSE로 대체한다 (`docs/api-spec.md`의 "미해결 사항" 참고).
3. **데이터 마이그레이션** — Firestore에 운영 데이터가 있다면 컷오버 직전 1회성 배치로 PostgreSQL로
   이관한다 (`docs/entity-design.md`의 "마이그레이션 / 컷오버 메모" 참고). 기존 공유 링크가 깨지지
   않도록 Firestore 문서 id를 그대로 유지한다.
4. **검증** — 스테이징 환경에서 BE API 기준으로 라인업 생성/공유/댓글/라커룸 전체 플로우를
   회귀 테스트한다.
5. **Firebase 제거** — 검증 완료 후 FE에서 `firebase` 패키지, `src/firebase/` 폴더, `.env`의
   `VITE_FIREBASE_*` 값을 제거하고 Firebase 콘솔 프로젝트를 비활성화/삭제한다. Google
   Analytics(`gtag`)는 Firebase와 무관한 별도 서비스라 이 제거 대상에 포함되지 않는다.

## 로컬 개발 환경 (Docker)

```bash
cp .env.example .env   # DB_NAME / DB_USERNAME / DB_PASSWORD 값 채우기
docker compose up --build
```

- `backend` 서비스: `localhost:8080`
- `postgres` 서비스: `localhost:5432`, 데이터는 named volume(`postgres_data`)에 영속화
- `application.yml`의 datasource는 환경변수(`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`,
  `DB_PASSWORD`) 기반이며 기본값이 `localhost:5432`라, Docker 없이 로컬에 PostgreSQL을 직접
  설치해 돌려도 그대로 동작한다.

## 빌드 / 테스트

```bash
./gradlew build
./gradlew test
./gradlew bootRun
```

## 코딩 컨벤션

- 엔티티는 세터를 열어두지 않고 생성자/도메인 메서드로 상태를 변경한다 (`@NoArgsConstructor(access = PROTECTED)` + 정적 팩토리 또는 빌더).
- 컨트롤러는 요청/응답 DTO만 다루고 엔티티를 직접 노출하지 않는다 (`docs/api-spec.md`의 응답 모델 기준).
- 소유권 검증(`ownerId`/`editToken` 비교)은 컨트롤러가 아닌 서비스 레이어에서 수행한다.
