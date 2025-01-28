package concert.domain.member.exceptions;

import concert.commons.common.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ErrorResponse> handleMemberException(MemberException ex) {

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMemberExceptionType().getHttpStatus(),
                ex.getMemberExceptionType().getMessage()
        );

        return ResponseEntity.status(ex.getMemberExceptionType().getHttpStatus()).body(errorResponse);
    }
}