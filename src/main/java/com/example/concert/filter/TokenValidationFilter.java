package com.example.concert.filter;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.redis.ActiveQueueDao;
import com.example.concert.redis.WaitingQueueDao;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenValidationFilter implements Filter {

    private final WaitingQueueDao waitingQueueDao;
    private final ActiveQueueDao activeQueueDao;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String requestURI = httpRequest.getRequestURI();

        if (isTokenValidationRequired(requestURI)) {
            String token = httpRequest.getHeader("token");
            long concertId = Long.parseLong(httpRequest.getHeader("concertId"));

            if (!validateToken(concertId, token)) {
                throw new CustomException(ErrorCode.NOT_VALID_TOKEN, Loggable.NEVER);
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isTokenValidationRequired(String requestURI) {
        List<String> validURIs = Arrays.asList(
                "/api/v1/concertSchedule",
                "/api/v1/reservation",
                "/api/v1/seat"
        );

        return validURIs.stream().anyMatch(requestURI::startsWith);
    }

    private boolean validateToken(long concertId, String token) {

        String uuid = token.split(":")[1];

        long rank = waitingQueueDao.getWaitingRank(concertId, uuid);

        if (rank != -1) {
            return true;
        }

        String result = activeQueueDao.getToken(concertId, uuid);

        if (result != null) {
            return true;
        }

        return false;
    }
}
