package org.example.lineupmaker_be.domain.common.exception;

// GlobalExceptionHandler가 403으로 매핑한다.
public class ForbiddenException extends DomainException {

    public ForbiddenException(String code, String message) {
        super(code, message);
    }

    // 소유자만 허용되는 동작(삭제, 편집 토큰 발급, 댓글 삭제 등)에서 사용
    public static ForbiddenException notOwner() {
        return new ForbiddenException("NOT_OWNER", "본인 소유의 리소스가 아닙니다.");
    }

    // 소유자이거나 유효한 editToken을 가진 경우에만 허용되는 동작(라인업 수정)에서 사용
    public static ForbiddenException notOwnerOrEditToken() {
        return new ForbiddenException(
                "NOT_OWNER_OR_EDIT_TOKEN",
                "본인 소유가 아니고 유효한 편집 토큰도 없습니다."
        );
    }
}
