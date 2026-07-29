package org.example.lineupmaker_be.web.dto.lineup;


import org.example.lineupmaker_be.domain.model.lineup.LineUpEntity;

// Vercel의 api/lineup-og.js가 OG 메타태그(og:title 등)를 만들 때 호출하는 공개 엔드포인트용 응답.
// teamName 외 다른 필드는 일부러 넣지 않는다 (공개 엔드포인트라 노출 최소화).
public record LineUpOgSummaryResponse(
        String teamName
) {
    public static LineUpOgSummaryResponse from(LineUpEntity entity) {
        return new LineUpOgSummaryResponse(entity.getTeamName());
    }
}
