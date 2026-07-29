package org.example.lineupmaker_be.web.dto.lineup;

import org.example.lineupmaker_be.domain.model.lineup.LineUpEntity;

import java.time.Instant;

public record LineUpSummaryResponse(
        String id,
        String teamName,
        Instant updatedAt
) {
    public static LineUpSummaryResponse from(LineUpEntity entity) {
        return new LineUpSummaryResponse(entity.getId(), entity.getTeamName(), entity.getUpdatedAt());
    }
}
