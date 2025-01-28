package concert.commons.interceptor;

import concert.commons.common.CustomException;
import concert.commons.common.ErrorCode;
import concert.commons.utils.TimeProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class TokenValidationInterceptor implements HandlerInterceptor {

    @Autowired
    private TimeProvider timeProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if(isTokenValidationRequired(request.getRequestURI())) {
            if (token == null) {
                throw new CustomException(ErrorCode.NOT_VALID_TOKEN);
            }
        }

        return true;
    }


    private boolean isTokenValidationRequired(String requestURI) {
        List<String> validURIs = Arrays.asList(
                "/concertSchedule",
                // "/reservation",
                "/seat"
        );

        return validURIs.stream().anyMatch(requestURI::startsWith);
    }

    private boolean isTenMinutesPassed(LocalDateTime updatedAt) {
        LocalDateTime now = timeProvider.now();
        Duration duration = Duration.between(updatedAt, now);
        return duration.toMinutes() >= 10;
    }
}
