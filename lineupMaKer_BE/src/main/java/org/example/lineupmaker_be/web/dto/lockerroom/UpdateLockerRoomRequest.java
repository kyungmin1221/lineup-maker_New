package org.example.lineupmaker_be.web.dto.lockerroom;

import org.example.lineupmaker_be.domain.common.PlayerJson;

import java.util.List;

public record UpdateLockerRoomRequest(
        String name,
        List<PlayerJson> players
) {
}
