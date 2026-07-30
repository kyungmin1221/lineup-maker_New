package org.example.lineupmaker_be.web.dto.lockerroom;

import org.example.lineupmaker_be.domain.common.PlayerJson;
import org.example.lineupmaker_be.domain.model.lockerroom.LockerRoom;

import java.time.Instant;
import java.util.List;

public record LockerRoomResponse(
        String id,
        String name,
        String ownerId,
        List<PlayerJson> players,
        Instant createdAt,
        Instant updatedAt
) {
    public static LockerRoomResponse from(LockerRoom entity) {
        return new LockerRoomResponse(
                entity.getId(), entity.getName(), entity.getOwnerId(),
                entity.getPlayers(), entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}
