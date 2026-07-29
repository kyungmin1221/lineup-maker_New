package org.example.lineupmaker_be.domain.lineup;

import java.util.List;
import java.util.Map;

public record QuarterJson(
        String id,
        String label,
        List<PlacedPlayerJson> players,
        List<CommentJson> comments,
        List<ScenarioJson> scenarios,
        Map<String, String> formations   // { "base": "4-3-3", "move": "..." }
) {
}
