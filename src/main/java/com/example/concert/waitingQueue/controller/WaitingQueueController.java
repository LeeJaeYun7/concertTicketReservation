package com.example.concert.waitingQueue.controller;

import com.example.concert.waitingQueue.dto.response.TokenResponse;
import com.example.concert.waitingQueue.dto.response.WaitingRankResponse;
import com.example.concert.waitingQueue.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WaitingQueueController {

    private final WaitingQueueService waitingQueueService;

    @GetMapping("/api/v1/waitingQueue/token")
    public ResponseEntity<TokenResponse> retrieveToken(@RequestParam(value="concertId") long concertId, @RequestParam(value="uuid") String uuid) {
        TokenResponse tokenResponse = waitingQueueService.retrieveToken(concertId, uuid);

        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }

    @GetMapping("/api/v1/waitingQueue/rank")
    public ResponseEntity<WaitingRankResponse> retrieveWaitingRank(@RequestParam(value="concertId") long concertId, @RequestParam(value="token") String token) {
        String uuid = token.split(":")[1];

        WaitingRankResponse waitingRankResponse = waitingQueueService.retrieveWaitingRank(concertId, uuid);

        return ResponseEntity.status(HttpStatus.CREATED).body(waitingRankResponse);
    }
}
