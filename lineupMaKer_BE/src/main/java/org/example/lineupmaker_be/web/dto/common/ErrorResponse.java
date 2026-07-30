package org.example.lineupmaker_be.web.dto.common;

// api-spec.md의 공통 에러 응답 모양 { "code": "...", "message": "..." }
public record ErrorResponse(String code, String message) {
}
