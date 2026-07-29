package org.example.lineupmaker_be.web.controller.lineup;

import lombok.RequiredArgsConstructor;
import org.example.lineupmaker_be.service.lineup.LineUpService;
import org.example.lineupmaker_be.web.lineup.dto.CommentRequest;
import org.example.lineupmaker_be.web.lineup.dto.CommentResponse;
import org.example.lineupmaker_be.web.lineup.dto.CreateLineUpRequest;
import org.example.lineupmaker_be.web.lineup.dto.EditTokenResponse;
import org.example.lineupmaker_be.web.lineup.dto.LineUpOgSummaryResponse;
import org.example.lineupmaker_be.web.lineup.dto.LineUpResponse;
import org.example.lineupmaker_be.web.lineup.dto.LineUpSummaryResponse;
import org.example.lineupmaker_be.web.lineup.dto.UpdateLineUpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 규칙: service에만 의존한다. 비즈니스 로직/소유권 검증은 여기서 하지 않는다.
// 이 클래스가 하는 일: HTTP 요청(경로변수/헤더/바디) 파싱 -> service 호출 -> 결과를 응답으로 변환. 그게 전부.
// 예외는 여기서 try/catch 하지 않는다 - service가 던진 NotFoundException/ForbiddenException은
// GlobalExceptionHandler(@RestControllerAdvice)가 잡아서 404/403 + ErrorResponse로 변환한다.
@RestController
@RequestMapping("/api/v1/lineups")
@RequiredArgsConstructor
public class LineUpController {

    private final LineUpService lineUpService;

    // TODO: POST /api/v1/lineups - 라인업 생성
    // - X-Device-Id 헤더 필수 (@RequestHeader("X-Device-Id") String deviceId)
    // - @Valid로 request 검증 (CreateLineUpRequest에 Bean Validation 어노테이션 필요)
    // - lineUpService.create(deviceId, request) 호출
    // - 성공 시 201 Created 상태코드로 응답 (ResponseEntity.status(HttpStatus.CREATED).body(...))
    @PostMapping
    public ResponseEntity<LineUpResponse> create() {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: GET /api/v1/lineups/{id} - 단건 조회 (공개, 헤더 불필요)
    // - lineUpService.get(id) 호출 결과를 그대로 반환 (200 OK)
    @GetMapping("/{id}")
    public LineUpResponse get(@PathVariable String id) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: GET /api/v1/lineups/{id}/summary - OG 메타태그용 요약 조회 (공개)
    // - lineUpService.getSummary(id) 호출 결과를 그대로 반환
    @GetMapping("/{id}/summary")
    public LineUpOgSummaryResponse getSummary(@PathVariable String id) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: PATCH /api/v1/lineups/{id} - 부분 수정 (자동저장)
    // - X-Device-Id, X-Edit-Token 둘 다 required = false - 소유권 검증은 service가 처리
    // - lineUpService.update(id, deviceId, editToken, request) 호출
    @PatchMapping("/{id}")
    public LineUpResponse update(@PathVariable String id) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: DELETE /api/v1/lineups/{id} - 삭제 (소유자만)
    // - X-Device-Id 헤더 필수
    // - lineUpService.delete(id, deviceId) 호출 후 204 No Content 응답
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: GET /api/v1/lineups/me - 내 라인업 목록
    // - X-Device-Id 헤더 필수
    // - lineUpService.findMine(deviceId) 호출 결과를 그대로 반환
    @GetMapping("/me")
    public List<LineUpSummaryResponse> findMine() {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: POST /api/v1/lineups/{id}/edit-token - 편집 토큰 발급/조회 (소유자만)
    // - X-Device-Id 헤더 필수
    // - lineUpService.getOrCreateEditToken(id, deviceId) 호출
    @PostMapping("/{id}/edit-token")
    public EditTokenResponse issueEditToken(@PathVariable String id) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: POST /api/v1/lineups/{id}/quarters/{quarterIdx}/comments - 댓글 추가 (공개, 헤더 불필요)
    // - @Valid로 request 검증 (CommentRequest에 name/text 빈 값 방지 어노테이션 필요)
    // - lineUpService.addComment(id, quarterIdx, request) 호출, 성공 시 201 Created
    @PostMapping("/{id}/quarters/{quarterIdx}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable String id, @PathVariable int quarterIdx) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: DELETE /api/v1/lineups/{id}/quarters/{quarterIdx}/comments/{commentIdx} - 댓글 삭제 (소유자만)
    // - X-Device-Id 헤더 필수
    // - lineUpService.deleteComment(id, quarterIdx, commentIdx, deviceId) 호출 후 204 No Content 응답
    @DeleteMapping("/{id}/quarters/{quarterIdx}/comments/{commentIdx}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String id, @PathVariable int quarterIdx, @PathVariable int commentIdx) {
        throw new UnsupportedOperationException("TODO");
    }
}
