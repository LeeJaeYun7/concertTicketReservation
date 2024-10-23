package com.example.concert.filter;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.utils.TimeProvider;
import com.example.concert.waitingQueue.domain.WaitingQueue;
import com.example.concert.waitingQueue.repository.WaitingQueueRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TokenValidationFilter implements Filter {

    private final TimeProvider timeProvider;
    private final WaitingQueueRepository waitingQueueRepository;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String token = httpRequest.getHeader("Authorization");

        if (!validateToken(token)) {
            throw new CustomException(ErrorCode.NOT_VALID_TOKEN, Loggable.NEVER);
        }

        chain.doFilter(request, response);
    }

    private boolean validateToken(String token) {
        Optional<WaitingQueue> waitingQueueOpt = waitingQueueRepository.findByToken(token);

        return waitingQueueOpt.isPresent();
    }
}
