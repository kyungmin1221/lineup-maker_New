package org.example.lineupmaker_be.domain.model.lineup;

import java.util.List;
import java.util.Map;

// 각 쿼터를 나타내는 record class
public record QuarterJson(
        String id,
        String label,       // 1쿼터 ..등
        List<PlacedPlayerJson> players,
        List<CommentJson> comments,     // 쿼터에 달린 댓글 여러 개
        List<ScenarioJson> scenarios,
        Map<String, String> formations   // { "base": "4-3-3", "move": "..." }
) {
}
