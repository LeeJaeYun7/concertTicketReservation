package concert.commons.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    PAYMENT_FAILED(HttpStatus.BAD_REQUEST,"결제가 실패했습니다."),

    //== 400 ==//
    NOT_VALID_TOKEN(HttpStatus.BAD_REQUEST,"유효한 토큰이 아닙니다."),

    HOST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 토큰을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}