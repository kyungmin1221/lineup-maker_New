package org.example.lineupmaker_be.web.dto.lockerroom;

import org.example.lineupmaker_be.domain.model.lockerroom.LockerRoom;

import java.time.Instant;

public record LockerRoomSummaryResponse(
        String id,
        String name,
        int playerCount,
        Instant updatedAt
) {
    public static LockerRoomSummaryResponse from(LockerRoom entity) {
        return new LockerRoomSummaryResponse(
                entity.getId(), entity.getName(), entity.getPlayers().size(), entity.getUpdatedAt()
        );
    }
}
