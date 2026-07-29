package org.example.lineupmaker_be.domain.common.exception;

import lombok.Getter;

// api-spec.md의 ErrorResponse(code, message)를 만들 때 필요한 코드를 예외 자체가 들고 다니게 한다.
// GlobalExceptionHandler(web 계층)가 이 code/message를 그대로 응답 바디로 옮겨 담는다.
@Getter
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }
}
