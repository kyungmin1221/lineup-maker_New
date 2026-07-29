package org.example.lineupmaker_be.web.dto.lineup;

import org.example.lineupmaker_be.domain.common.PlayerJson;
import org.example.lineupmaker_be.domain.model.lineup.LineUpEntity;
import org.example.lineupmaker_be.domain.model.lineup.QuarterJson;

import java.time.Instant;
import java.util.List;

public record LineUpResponse(
        String id,
        String teamName,
        String ownerId,
        boolean showOpponents,
        List<PlayerJson> squad,
        List<QuarterJson> quarters,
        Instant createdAt,
        Instant updatedAt
) {
    public static LineUpResponse from(LineUpEntity entity) {
        return new LineUpResponse(
                entity.getId(), entity.getTeamName(), entity.getOwnerId(),
                entity.isShowOpponents(), entity.getSquad(), entity.getQuarters(),
                entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}