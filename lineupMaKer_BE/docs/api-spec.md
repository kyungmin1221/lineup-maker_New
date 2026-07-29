# API 스펙

Base path: `/api/v1`
데이터 포맷: JSON (`application/json`)
엔티티 설계는 [`entity-design.md`](./entity-design.md) 참고.

> 이 API는 Firebase(Firestore + Anonymous Auth)를 **완전히 대체**하는 것을 목표로 설계됐다.
> 컷오버 이후 FE는 `src/firebase/*` 대신 이 API만 호출하며, Firestore와 영구 병행 운영하지
> 않는다. 컷오버 단계는 `CLAUDE.md`의 "Firebase 제거 컷오버 계획" 참고.

## 인증 헤더

Firebase Anonymous Auth를 완전히 대체한다. 컷오버 이후 FE는 Firebase Auth를 더 이상 호출하지
않고, 최초 진입 시 자체적으로 발급해 로컬(localStorage)에 보관하는 device id를 매 요청에 헤더로
실어 보낸다. 서버는 이 값을 `ownerId`로 신뢰한다.

```
X-Device-Id: <client-generated-uuid>
```

- 소유권 확인이 필요 없는 공개 조회(`GET /lineups/{id}` 등)는 헤더 없이도 호출 가능.
- 쓰기 요청 중 소유자 전용 동작(삭제, edit-token 발급, 댓글 삭제)은 `X-Device-Id`가 `owner_id`와
  일치해야 함. 불일치 시 `403 FORBIDDEN`.
- 공유 편집 링크로 들어온 비소유자의 쓰기 요청은 `X-Edit-Token` 헤더로 검증한다.

```
X-Edit-Token: <lineup.editToken>
```

> 이 방식은 Firebase 시절과 동일한 신뢰 수준(클라이언트가 제시한 값을 그대로 신뢰)이다.
> 더 강한 위변조 방지가 필요해지면 `X-Device-Id` 발급을 서버 주도 JWT로 교체할 수 있도록
> 인증 로직은 서비스 레이어에서 한 곳(`OwnershipValidator` 등)에 모아둔다.

## 공통 에러 응답

```json
{
  "code": "LINEUP_NOT_FOUND",
  "message": "라인업을 찾을 수 없습니다."
}
```

| 상태 코드 | 상황 |
|---|---|
| 400 | 요청 바디 검증 실패 |
| 403 | 소유자/편집 토큰 불일치 |
| 404 | 리소스 없음 |

---

## Lineups

### `POST /lineups` — 라인업 생성
헤더: `X-Device-Id` (필수, ownerId로 저장)

Request (`CreateLineupRequest`)
```json
{
  "teamName": "이름없음 FC",
  "squad": [{ "id": "1", "name": "유상엽", "number": "1" }],
  "quarters": [{ "id": "q1", "label": "1쿼터", "players": [], "comments": [], "scenarios": [] }]
}
```

Response `201` (`LineupResponse`) — 아래 "공통 응답 모델" 참고.

---

### `GET /lineups/{id}` — 단건 조회 (공개)
인증 불필요. 뷰 페이지/편집 페이지 진입 시 사용.

Response `200` (`LineupResponse`) / `404`

---

### `GET /lineups/{id}/summary` — OG 메타태그용 요약 (공개)
Vercel serverless(`api/lineup-og.js`)가 Firestore REST를 직접 호출하던 부분을 대체.

Response `200`
```json
{ "teamName": "이름없음 FC" }
```

---

### `PATCH /lineups/{id}` — 부분 수정 (자동저장)
헤더: `X-Device-Id` **또는** `X-Edit-Token` 중 하나가 소유권 검증을 통과해야 함.
모든 필드는 optional — 보낸 필드만 갱신 (Firestore `updateDoc`과 동일한 partial update 의미론).

Request (`UpdateLineupRequest`)
```json
{
  "teamName": "FC 서울",
  "squad": [ /* ... */ ],
  "quarters": [ /* ... */ ],
  "showOpponents": false
}
```

Response `200` (`LineupResponse`) / `403` / `404`

---

### `DELETE /lineups/{id}` — 삭제
헤더: `X-Device-Id` (소유자만 가능)

Response `204` / `403` / `404`

---

### `GET /lineups/me` — 내 라인업 목록
헤더: `X-Device-Id` (필수)

Response `200` (`List<LineupSummaryResponse>`), 최근 수정순 정렬
```json
[
  { "id": "abc123", "teamName": "FC 서울", "updatedAt": "2026-07-29T10:00:00Z" }
]
```

---

### `POST /lineups/{id}/edit-token` — 편집 토큰 발급/조회
헤더: `X-Device-Id` (소유자만 가능). 이미 있으면 기존 토큰 반환, 없으면 새로 생성.

Response `200`
```json
{ "editToken": "9f1c2a7e0b3d4f5a6c7d8e9f" }
```

---

### `POST /lineups/{id}/quarters/{quarterIdx}/comments` — 댓글 추가
헤더: 없어도 됨 (뷰어도 작성 가능, `Comments.jsx`와 동일한 정책)

Request (`CommentRequest`)
```json
{ "name": "김철수", "text": "이 배치 좋네요" }
```

Response `201` (`CommentResponse`)
```json
{ "name": "김철수", "text": "이 배치 좋네요", "createdAt": "2026-07-29T10:00:00Z" }
```

---

### `DELETE /lineups/{id}/quarters/{quarterIdx}/comments/{commentIdx}` — 댓글 삭제
헤더: `X-Device-Id` (소유자만 가능, `ViewPage.jsx`의 `isOwner` 정책과 동일)

Response `204` / `403`

---

## Locker Rooms

### `POST /locker-rooms` — 생성
헤더: `X-Device-Id`

Request
```json
{ "name": "새 라커룸" }
```

Response `201` (`LockerRoomResponse`)

---

### `GET /locker-rooms/{id}` — 조회 (공개)
Response `200` (`LockerRoomResponse`) / `404`

---

### `PATCH /locker-rooms/{id}` — 수정
헤더: `X-Device-Id` (소유자만 가능)

Request
```json
{ "name": "1군 라커룸", "players": [{ "id": "p1", "name": "이주호", "number": "2" }] }
```

Response `200` (`LockerRoomResponse`) / `403`

---

### `DELETE /locker-rooms/{id}` — 삭제
헤더: `X-Device-Id` (소유자만 가능)

Response `204` / `403`

---

### `GET /locker-rooms/me` — 내 라커룸 목록
헤더: `X-Device-Id`

Response `200` (`List<LockerRoomSummaryResponse>`)
```json
[{ "id": "lr1", "name": "1군 라커룸", "playerCount": 15, "updatedAt": "2026-07-29T10:00:00Z" }]
```

---

## 공통 응답 모델

```json
// LineupResponse
{
  "id": "abc123",
  "teamName": "FC 서울",
  "ownerId": "device-uuid",
  "showOpponents": true,
  "squad": [ { "id": "1", "name": "유상엽", "number": "1" } ],
  "quarters": [
    {
      "id": "q1",
      "label": "1쿼터",
      "players": [ { "playerId": "1", "x": 50, "y": 90 } ],
      "comments": [ { "name": "김철수", "text": "굿", "createdAt": "2026-07-29T10:00:00Z" } ],
      "scenarios": [
        {
          "id": "s1",
          "label": "움직임 1",
          "steps": [
            {
              "id": "st1",
              "players": [ { "playerId": "1", "x": 50, "y": 90 } ],
              "opponents": [ { "id": "opp-0", "x": 50, "y": 10 } ],
              "ball": { "x": 50, "y": 50 }
            }
          ]
        }
      ],
      "formations": { "base": "4-3-3" }
    }
  ],
  "createdAt": "2026-07-29T09:00:00Z",
  "updatedAt": "2026-07-29T10:00:00Z"
}
```

> `editToken`은 응답에 포함하지 않는다 — 소유자가 명시적으로 `POST /lineups/{id}/edit-token`을
> 호출했을 때만 노출 (Firestore 문서 전체를 그대로 내려주던 기존 방식보다 안전).

```json
// LineupSummaryResponse
{ "id": "abc123", "teamName": "FC 서울", "updatedAt": "2026-07-29T10:00:00Z" }
```

```json
// LockerRoomResponse
{
  "id": "lr1",
  "name": "1군 라커룸",
  "ownerId": "device-uuid",
  "players": [ { "id": "p1", "name": "이주호", "number": "2" } ],
  "createdAt": "2026-07-29T09:00:00Z",
  "updatedAt": "2026-07-29T10:00:00Z"
}
```

---

## 미해결 사항 (추후 결정 필요)

- **실시간 댓글 동기화**: Firestore `onSnapshot`을 대체할 방법. 1차로는 FE가 짧은 주기로
  `GET /lineups/{id}` 폴링, 필요시 `GET /lineups/{id}/stream` (SSE)으로 전환 검토.
- **인증 강화**: `X-Device-Id` 신뢰 방식 → 서버 발급 JWT로 전환 여부.
- **레이트 리밋**: 댓글 작성처럼 인증 없는 공개 쓰기 엔드포인트는 어뷰징 방지책 필요.
