package org.example.lineupmaker_be.web.controller.lockerroom;

import lombok.RequiredArgsConstructor;
import org.example.lineupmaker_be.service.lockerroom.LockerRoomService;
import org.example.lineupmaker_be.web.dto.lockerroom.CreateLockerRoomRequest;
import org.example.lineupmaker_be.web.dto.lockerroom.LockerRoomResponse;
import org.example.lineupmaker_be.web.dto.lockerroom.LockerRoomSummaryResponse;
import org.example.lineupmaker_be.web.dto.lockerroom.UpdateLockerRoomRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 규칙: service에만 의존한다. 비즈니스 로직/소유권 검증은 여기서 하지 않는다.
@RestController
@RequestMapping("/api/v1/locker-rooms")
@RequiredArgsConstructor
public class LockerRoomController {

    private final LockerRoomService lockerRoomService;

    // POST /api/v1/locker-rooms - 라커룸 생성
    @PostMapping
    public ResponseEntity<LockerRoomResponse> create(@RequestHeader("X-Device-Id") String deviceId,
                                                     @RequestBody CreateLockerRoomRequest request) {
        LockerRoomResponse response = lockerRoomService.create(deviceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/v1/locker-rooms/{id} - 단건 조회 (공개, 헤더 불필요)
    @GetMapping("/{id}")
    public ResponseEntity<LockerRoomResponse> getLockerRoom(@PathVariable String id) {
        LockerRoomResponse response = lockerRoomService.getLockerRoom(id);
        return ResponseEntity.ok(response);
    }

    // PATCH /api/v1/locker-rooms/{id} - 수정 (소유자만)
    @PatchMapping("/{id}")
    public ResponseEntity<LockerRoomResponse> update(@PathVariable String id,
                                                     @RequestHeader("X-Device-Id") String deviceId,
                                                     @RequestBody UpdateLockerRoomRequest request) {
        LockerRoomResponse response = lockerRoomService.update(id, deviceId, request);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/locker-rooms/{id} - 삭제 (소유자만)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id,
                                       @RequestHeader("X-Device-Id") String deviceId) {
        lockerRoomService.delete(id, deviceId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/locker-rooms/me - 내 라커룸 목록
    @GetMapping("/me")
    public ResponseEntity<List<LockerRoomSummaryResponse>> findMine(@RequestHeader("X-Device-Id") String deviceId) {
        List<LockerRoomSummaryResponse> response = lockerRoomService.findMine(deviceId);
        return ResponseEntity.ok(response);
    }
}
