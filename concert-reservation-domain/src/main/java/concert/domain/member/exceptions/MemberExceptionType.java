package concert.domain.member.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberExceptionType {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    MemberExceptionType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
