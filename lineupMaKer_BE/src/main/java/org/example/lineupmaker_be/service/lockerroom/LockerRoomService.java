package org.example.lineupmaker_be.service.lockerroom;

import lombok.RequiredArgsConstructor;
import org.example.lineupmaker_be.domain.common.exception.ForbiddenException;
import org.example.lineupmaker_be.domain.common.exception.NotFoundException;
import org.example.lineupmaker_be.domain.model.lockerroom.LockerRoom;
import org.example.lineupmaker_be.domain.repo.LockerRoomRepository;
import org.example.lineupmaker_be.web.dto.lockerroom.CreateLockerRoomRequest;
import org.example.lineupmaker_be.web.dto.lockerroom.LockerRoomResponse;
import org.example.lineupmaker_be.web.dto.lockerroom.LockerRoomSummaryResponse;
import org.example.lineupmaker_be.web.dto.lockerroom.UpdateLockerRoomRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 규칙: domain에만 의존한다. web(Controller)을 참조하지 않는다.
@Service
@RequiredArgsConstructor
public class LockerRoomService {

    private final LockerRoomRepository lockerRoomRepository;

    @Transactional
    public LockerRoomResponse create(String deviceId, CreateLockerRoomRequest request) {
        LockerRoom lockerRoom = LockerRoom.create(request.name(), deviceId);
        LockerRoom saved = lockerRoomRepository.save(lockerRoom);
        return LockerRoomResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public LockerRoomResponse get(String id) {
        LockerRoom lockerRoom = findEntityOrThrow(id);
        return LockerRoomResponse.from(lockerRoom);
    }

    @Transactional
    public LockerRoomResponse update(String id, String deviceId, UpdateLockerRoomRequest request) {
        LockerRoom lockerRoom = findEntityOrThrow(id);
        validateOwner(deviceId, lockerRoom);

        // request에서 null이 아닌 필드만 반영
        if (request.name() != null) {
            lockerRoom.updateName(request.name());
        }
        if (request.players() != null) {
            lockerRoom.updatePlayers(request.players());
        }

        LockerRoom updated = lockerRoomRepository.save(lockerRoom);
        return LockerRoomResponse.from(updated);
    }

    @Transactional
    public void delete(String id, String deviceId) {
        LockerRoom lockerRoom = findEntityOrThrow(id);
        validateOwner(deviceId, lockerRoom);
        lockerRoomRepository.delete(lockerRoom);
    }

    @Transactional(readOnly = true)
    public List<LockerRoomSummaryResponse> findMine(String deviceId) {
        List<LockerRoom> lockerRooms = lockerRoomRepository.findByOwnerIdOrderByUpdatedAtDesc(deviceId);
        return lockerRooms.stream()
                .map(LockerRoomSummaryResponse::from)
                .toList();
    }

    private void validateOwner(String deviceId, LockerRoom lockerRoom) {
        if (!lockerRoom.getOwnerId().equals(deviceId)) {
            throw ForbiddenException.notOwner();
        }
    }

    private LockerRoom findEntityOrThrow(String id) {
        return lockerRoomRepository.findById(id)
                .orElseThrow(() -> NotFoundException.lockerRoom(id));
    }
}
