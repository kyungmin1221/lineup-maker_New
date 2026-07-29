# JPA 엔티티 설계

lineupMaker의 데이터는 기존 Firestore(`lineups`, `lockerRooms` 컬렉션)에서 PostgreSQL로 이관된다.
이 이관은 **완전 대체**가 목표다 — 백엔드가 서비스 가능한 시점에 FE를 이 API로 전환하는 컷오버를
거쳐 Firebase(Firestore + Anonymous Auth)를 완전히 제거한다. Firestore와 PostgreSQL을 영구적으로
병행 운영하지 않는다 (컷오버 단계는 `CLAUDE.md`의 "Firebase 제거 컷오버 계획" 참고).

쿼터/시나리오/스텝처럼 깊게 중첩되고 스키마가 자주 바뀌는 구조를 전부 정규화하면 테이블 수와 JOIN
비용이 과도해지므로, **자주 조회·필터링에 쓰이는 필드만 정규 컬럼으로 두고 나머지 중첩 구조는
PostgreSQL `jsonb` 컬럼**에 저장하는 하이브리드 방식을 택한다.

## 1. Lineup

`lineups` 테이블 — Firestore `lineups` 컬렉션에 대응.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| `id` | `varchar` (PK) | UUID 또는 짧은 랜덤 문자열. 서버가 생성 |
| `team_name` | `varchar` | 팀 이름 |
| `owner_id` | `varchar` | 소유자 식별자 (device id). 인덱스 필요 |
| `edit_token` | `varchar`, nullable | 공유 편집 링크용 토큰. 최초 공유 시점에 생성 |
| `show_opponents` | `boolean` | 상대팀 표시 여부, 기본 `true` |
| `squad` | `jsonb` | 선수단 목록 |
| `quarters` | `jsonb` | 쿼터별 배치/시나리오/댓글 |
| `created_at` | `timestamp` | 생성 시각 |
| `updated_at` | `timestamp` | 수정 시각 (자동저장마다 갱신) |

인덱스: `idx_lineups_owner_id (owner_id)` — "내 라인업 목록" 조회용.

### JPA 엔티티

```java
@Entity
@Table(name = "lineups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lineup {

    @Id
    private String id;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "edit_token")
    private String editToken;

    @Column(name = "show_opponents", nullable = false)
    private boolean showOpponents = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<PlayerJson> squad = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<QuarterJson> quarters = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
```

### `quarters` jsonb 내부 구조 (POJO, 엔티티 아님)

Firestore 문서 구조를 그대로 옮긴 것으로, `useLineup.js`의 상태 모양과 1:1로 맞춘다.

```java
record PlayerJson(String id, String name, String number) {}

record QuarterJson(
    String id,
    String label,
    List<PlacedPlayerJson> players,
    List<CommentJson> comments,
    List<ScenarioJson> scenarios,
    Map<String, String> formations   // { "base": "4-3-3", "move": "..." }
) {}

record PlacedPlayerJson(String playerId, double x, double y, String label) {}

record CommentJson(String name, String text, long createdAt) {}

record ScenarioJson(String id, String label, List<StepJson> steps) {}

record StepJson(
    String id,
    List<PlacedPlayerJson> players,
    List<OpponentJson> opponents,
    BallJson ball
) {}

record OpponentJson(String id, double x, double y) {}

record BallJson(double x, double y) {}
```

> 댓글 추가/삭제(`addComment`, `deleteComment`)처럼 `quarters` 배열의 특정 인덱스만 바꾸는 연산은
> jsonb 전체를 읽고 갱신하는 것보다, 서비스 레이어에서 엔티티를 로드해 리스트를 수정한 뒤
> `save()`하는 방식으로 처리한다 (Firestore의 `getDoc` → 수정 → `updateDoc` 패턴과 동일).

## 2. LockerRoom

`locker_rooms` 테이블 — Firestore `lockerRooms` 컬렉션에 대응.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| `id` | `varchar` (PK) | 서버 생성 |
| `name` | `varchar` | 라커룸 이름 |
| `owner_id` | `varchar` | 소유자 식별자, 인덱스 필요 |
| `players` | `jsonb` | `List<PlayerJson>` |
| `created_at` | `timestamp` | |
| `updated_at` | `timestamp` | |

인덱스: `idx_locker_rooms_owner_id (owner_id)`

```java
@Entity
@Table(name = "locker_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LockerRoom {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<PlayerJson> players = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
```

## 3. ID / 토큰 생성

Firestore 클라이언트 코드의 `generateId()` (랜덤 8자), `generateToken()` (12바이트 hex)을 서버로 이관:

- `id`: `UUID.randomUUID().toString()` 사용 권장 (충돌 걱정 없음). 기존 8자 짧은 id와 다르지만
  URL에는 그대로 노출되어도 무방 — FE 라우팅(`/edit/:id`, `/view/:id`)은 문자열이면 무엇이든 수용.
- `editToken`: `SecureRandom` 기반 24자 hex 문자열. `POST /lineups/{id}/edit-token` 호출 시점에
  없으면 생성해서 저장 (Firestore `getOrCreateEditToken`과 동일 동작).

## 4. 소유권/인가 모델

- `owner_id`는 클라이언트가 최초 발급받아 로컬에 저장하는 device id (자세한 인증 전략은
  `CLAUDE.md`의 "인증 전략" 절 참고).
- 쓰기 요청(`PATCH/DELETE`)은 서비스 레이어에서 반드시 `ownerId` 일치 또는 `editToken` 일치를
  검증한 뒤 처리한다. 지금까지 FE(Firestore 보안 규칙 의존)에서 느슨하게 처리되던 부분을
  서버가 강제하는 지점이다.
- 댓글 작성은 소유자·뷰어 누구나 가능, 댓글 삭제는 소유자만 가능 (`ViewPage.jsx`의 `isOwner` 로직과 동일).

## 5. 마이그레이션 / 컷오버 메모

- Hibernate `ddl-auto: update`로 초기 개발 단계에서는 자동 스키마 생성을 쓰되, 운영 전환 시점에는
  Flyway/Liquibase로 전환해 마이그레이션 이력을 관리하는 것을 권장.
- Firestore에 이미 쌓인 운영 데이터가 있다면, 컷오버 직전에 `lineups`/`lockerRooms` 컬렉션을
  export → 위 엔티티 구조에 맞게 변환하는 1회성 배치 스크립트로 옮긴다 (본 문서 범위 밖, 별도
  이슈로 관리). 이 배치가 끝나야 컷오버를 진행할 수 있다.
- Firestore의 8자 랜덤 `id`와 새 백엔드의 UUID `id`는 형식이 다르다 — 마이그레이션 시 기존 id를
  그대로 유지할지, 새 UUID로 재발급할지 결정 필요. 기존 공유 링크(`/view/:id`, `/edit/:id?token=`)가
  깨지지 않으려면 **기존 id를 그대로 유지**하는 쪽을 권장.
- 컷오버가 끝나면 Firestore/Firebase Anonymous Auth는 더 이상 참조하지 않는다. FE의
  `src/firebase/` 폴더, `firebase` 패키지, `.env`의 `VITE_FIREBASE_*` 값, Firebase 콘솔 프로젝트까지
  순서대로 제거한다 (전체 순서는 `CLAUDE.md`의 "Firebase 제거 컷오버 계획" 참고).
