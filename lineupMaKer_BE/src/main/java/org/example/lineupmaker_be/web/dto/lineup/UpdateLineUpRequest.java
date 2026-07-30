package org.example.lineupmaker_be.web.dto.lineup;

import org.example.lineupmaker_be.domain.common.PlayerJson;
import org.example.lineupmaker_be.domain.model.lineup.QuarterJson;

import java.util.List;

public record UpdateLineUpRequest(
    String deviceId,
    String editToken,
    String teamName,
    List<PlayerJson> squad,
    List<QuarterJson> quarters,
    Boolean showOpponents
) {
}
