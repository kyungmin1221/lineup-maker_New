package org.example.lineupmaker_be.web.controller.lineup;

import lombok.RequiredArgsConstructor;
import org.example.lineupmaker_be.service.lineup.LineUpService;
import org.example.lineupmaker_be.web.dto.lineup.*;
import org.springframework.http.HttpStatus;
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

    // POST /api/v1/lineups - 라인업 생성
    // deviceId는 헤더가 아니라 CreateLineUpRequest 안에 들어있음 (현재 서비스 설계 기준)
    @PostMapping
    public ResponseEntity<LineUpResponse> create(@RequestBody CreateLineUpRequest request) {
        LineUpResponse response = lineUpService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/v1/lineups/{id} - 단건 조회 (공개, 헤더 불필요)
    @GetMapping("/{id}")
    public ResponseEntity<LineUpResponse> get(@PathVariable String id) {
        LineUpResponse response = lineUpService.getLineUp(id);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/lineups/{id}/summary - OG 메타태그용 요약 조회 (공개)
    @GetMapping("/{id}/summary")
    public ResponseEntity<LineUpOgSummaryResponse> getSummary(@PathVariable String id) {
        LineUpOgSummaryResponse response = lineUpService.getSummary(id);
        return ResponseEntity.ok(response);
    }

    // PATCH /api/v1/lineups/{id} - 부분 수정 (자동저장)
    // deviceId/editToken도 헤더가 아니라 UpdateLineUpRequest 안에 들어있음 (현재 서비스 설계 기준)
    @PatchMapping("/{id}")
    public ResponseEntity<LineUpResponse> update(@PathVariable String id,
                                                  @RequestBody UpdateLineUpRequest request) {
        LineUpResponse response = lineUpService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/lineups/{id} - 삭제 (소유자만)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id,
                                        @RequestHeader("X-Device-Id") String deviceId) {
        lineUpService.delete(id, deviceId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/lineups/me - 내 라인업 목록
    @GetMapping("/me")
    public ResponseEntity<List<LineUpSummaryResponse>> findMine(@RequestHeader("X-Device-Id") String deviceId) {
        List<LineUpSummaryResponse> response = lineUpService.findMine(deviceId);
        return ResponseEntity.ok(response);
    }

    // POST /api/v1/lineups/{id}/edit-token - 편집 토큰 발급/조회 (소유자만)
    @PostMapping("/{id}/edit-token")
    public ResponseEntity<EditTokenResponse> issueEditToken(@PathVariable String id,
                                                             @RequestHeader("X-Device-Id") String deviceId) {
        EditTokenResponse response = lineUpService.getOrCreateEditToken(id, deviceId);
        return ResponseEntity.ok(response);
    }

    // POST /api/v1/lineups/{id}/quarters/{quarterIdx}/comments - 댓글 추가 (공개, 헤더 불필요)
    @PostMapping("/{id}/quarters/{quarterIdx}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable String id,
                                                       @PathVariable int quarterIdx,
                                                       @RequestBody CommentRequest request) {
        CommentResponse response = lineUpService.addComment(id, quarterIdx, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // DELETE /api/v1/lineups/{id}/quarters/{quarterIdx}/comments/{commentIdx} - 댓글 삭제 (소유자만)
    @DeleteMapping("/{id}/quarters/{quarterIdx}/comments/{commentIdx}")
    public ResponseEntity<Void> deleteComment(@PathVariable String id,
                                               @PathVariable int quarterIdx,
                                               @PathVariable int commentIdx,
                                               @RequestHeader("X-Device-Id") String deviceId) {
        lineUpService.deleteComment(id, quarterIdx, commentIdx, deviceId);
        return ResponseEntity.noContent().build();
    }
}
