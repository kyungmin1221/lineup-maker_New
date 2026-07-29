package org.example.lineupmaker_be.service.lockerroom;

import lombok.RequiredArgsConstructor;
import org.example.lineupmaker_be.domain.lockerroom.LockerRoomRepository;
import org.example.lineupmaker_be.web.lockerroom.dto.CreateLockerRoomRequest;
import org.example.lineupmaker_be.web.lockerroom.dto.LockerRoomResponse;
import org.example.lineupmaker_be.web.lockerroom.dto.LockerRoomSummaryResponse;
import org.example.lineupmaker_be.web.lockerroom.dto.UpdateLockerRoomRequest;
import org.springframework.stereotype.Service;

import java.util.List;

// 규칙: domain에만 의존한다. web(Controller)을 참조하지 않는다.
@Service
@RequiredArgsConstructor
public class LockerRoomService {

    private final LockerRoomRepository lockerRoomRepository;

    // TODO: 라커룸 생성
    // 1. LockerRoom 엔티티를 새로 만든다 (name은 request 값, ownerId는 deviceId, players는 빈 리스트로 시작)
    //    - 엔티티에 세터가 없으므로 정적 팩토리 메서드(예: LockerRoom.create(...))를 엔티티에 추가하는 걸 고려
    // 2. lockerRoomRepository.save(entity) 호출
    // 3. 저장된 엔티티를 LockerRoomResponse로 변환해서 반환
    public LockerRoomResponse create(String deviceId, CreateLockerRoomRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: 라커룸 단건 조회 (공개, 인증 불필요)
    // 1. lockerRoomRepository.findById(id), 없으면 NotFoundException
    // 2. LockerRoomResponse로 변환해서 반환
    public LockerRoomResponse get(String id) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: 라커룸 수정 (소유자만 가능)
    // 1. lockerRoomRepository.findById(id), 없으면 NotFoundException
    // 2. 소유권 검증 (deviceId == entity.ownerId), 아니면 ForbiddenException
    // 3. request에서 null이 아닌 필드만 반영 (name/players 둘 다 optional)
    //    - LockerRoomPage.jsx처럼 name은 blur 시, players는 800ms debounce 후 각각 저장될 수 있음을 감안
    // 4. 저장 후 LockerRoomResponse로 변환해서 반환
    public LockerRoomResponse update(String id, String deviceId, UpdateLockerRoomRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: 라커룸 삭제 (소유자만 가능)
    // 1. lockerRoomRepository.findById(id), 없으면 NotFoundException
    // 2. 소유권 검증
    // 3. lockerRoomRepository.delete(entity)
    public void delete(String id, String deviceId) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: 내 라커룸 목록 조회 (최근 수정순)
    // 1. LockerRoomRepository에 findByOwnerIdOrderByUpdatedAtDesc(String ownerId) 쿼리 메서드 추가
    // 2. 결과를 List<LockerRoomSummaryResponse>(id, name, playerCount, updatedAt)로 변환해서 반환
    //    - playerCount는 entity.getPlayers().size()로 계산
    public List<LockerRoomSummaryResponse> findMine(String deviceId) {
        throw new UnsupportedOperationException("TODO");
    }
}
