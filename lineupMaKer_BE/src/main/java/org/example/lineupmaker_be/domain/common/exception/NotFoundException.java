package org.example.lineupmaker_be.domain.common.exception;

// GlobalExceptionHandler가 404로 매핑한다.
public class NotFoundException extends DomainException {

    public NotFoundException(String code, String message) {
        super(code, message);
    }

    public static NotFoundException lineup(String id) {
        return new NotFoundException("LINEUP_NOT_FOUND", "라인업을 찾을 수 없습니다. id=" + id);
    }

    public static NotFoundException lockerRoom(String id) {
        return new NotFoundException("LOCKER_ROOM_NOT_FOUND", "라커룸을 찾을 수 없습니다. id=" + id);
    }
}
