package com.example.concert.waitingQueue.service;

import com.example.concert.member.service.MemberService;
import com.example.concert.waitingQueue.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitingQueueFacade {

    private final MemberService memberService;
    private final WaitingQueueService waitingQueueService;

    public TokenResponse retrieveToken(long concertId, String uuid) {
        memberService.getMemberByUuid(uuid);

        String token = waitingQueueService.retrieveToken(concertId, uuid);
        return TokenResponse.of(token);
    }
}
