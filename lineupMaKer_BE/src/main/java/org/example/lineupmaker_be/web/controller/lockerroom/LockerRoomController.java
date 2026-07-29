package org.example.lineupmaker_be.web.controller.lockerroom;

import lombok.RequiredArgsConstructor;
import org.example.lineupmaker_be.service.lockerroom.LockerRoomService;
import org.example.lineupmaker_be.web.lockerroom.dto.CreateLockerRoomRequest;
import org.example.lineupmaker_be.web.lockerroom.dto.LockerRoomResponse;
import org.example.lineupmaker_be.web.lockerroom.dto.LockerRoomSummaryResponse;
import org.example.lineupmaker_be.web.lockerroom.dto.UpdateLockerRoomRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 규칙: service에만 의존한다. 비즈니스 로직/소유권 검증은 여기서 하지 않는다.
@RestController
@RequestMapping("/api/v1/locker-rooms")
@RequiredArgsConstructor
public class LockerRoomController {

    private final LockerRoomService lockerRoomService;

    // TODO: POST /api/v1/locker-rooms - 라커룸 생성
    // - X-Device-Id 헤더 필수
    // - lockerRoomService.create(deviceId, request) 호출, 성공 시 201 Created
    @PostMapping
    public ResponseEntity<LockerRoomResponse> create() {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: GET /api/v1/locker-rooms/{id} - 단건 조회 (공개, 헤더 불필요)
    // - lockerRoomService.get(id) 호출 결과를 그대로 반환
    @GetMapping("/{id}")
    public LockerRoomResponse get(@PathVariable String id) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: PATCH /api/v1/locker-rooms/{id} - 수정 (소유자만)
    // - X-Device-Id 헤더 필수
    // - lockerRoomService.update(id, deviceId, request) 호출
    @PatchMapping("/{id}")
    public LockerRoomResponse update(@PathVariable String id) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: DELETE /api/v1/locker-rooms/{id} - 삭제 (소유자만)
    // - X-Device-Id 헤더 필수
    // - lockerRoomService.delete(id, deviceId) 호출 후 204 No Content 응답
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: GET /api/v1/locker-rooms/me - 내 라커룸 목록
    // - X-Device-Id 헤더 필수
    // - lockerRoomService.findMine(deviceId) 호출 결과를 그대로 반환
    @GetMapping("/me")
    public List<LockerRoomSummaryResponse> findMine() {
        throw new UnsupportedOperationException("TODO");
    }
}
