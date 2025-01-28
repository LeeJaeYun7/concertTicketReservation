package concert.domain.member.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberException extends RuntimeException{
    private final MemberExceptionType memberExceptionType;
}
