package org.example.lineupmaker_be.web.dto.lineup;

import org.example.lineupmaker_be.domain.model.lineup.LineUpEntity;

public record EditTokenResponse(String editToken)
{
    public static EditTokenResponse from(LineUpEntity entity) {
        return new EditTokenResponse(entity.getEditToken());
    }
}