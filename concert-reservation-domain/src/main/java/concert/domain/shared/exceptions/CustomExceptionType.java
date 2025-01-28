package concert.domain.shared.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CustomExceptionType {
    NOT_VALID_TOKEN(HttpStatus.BAD_REQUEST,"유효한 토큰이 아닙니다."),
    HOST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 호스트를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    CustomExceptionType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

}
