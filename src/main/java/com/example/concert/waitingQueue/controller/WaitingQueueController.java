package com.example.concert.waitingQueue.controller;

import com.example.concert.waitingQueue.dto.response.TokenResponse;
import com.example.concert.waitingQueue.service.WaitingQueueFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WaitingQueueController {

    private final WaitingQueueFacade waitingQueueFacade;

    @GetMapping("/waitingQueue/concert")
    public ResponseEntity<TokenResponse> retrieveWaitingRankOrToken(@RequestParam(value="concertId") long concertId, @RequestParam(value="uuid") String uuid) {
        TokenResponse tokenResponse = waitingQueueFacade.retrieveWaitingRankOrToken(concertId, uuid);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }
}
