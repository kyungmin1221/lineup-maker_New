package org.example.lineupmaker_be.domain.model.lineup;

import java.util.List;

public record StepJson(
        String id,
        List<PlacedPlayerJson> players,
        List<OpponentJson> opponents,
        BallJson ball
) {
}
