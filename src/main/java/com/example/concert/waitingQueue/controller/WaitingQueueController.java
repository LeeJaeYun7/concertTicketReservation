package com.example.concert.waitingQueue.controller;

import com.example.concert.waitingQueue.dto.request.TokenRequest;
import com.example.concert.waitingQueue.dto.response.WaitingNumberResponse;
import com.example.concert.waitingQueue.dto.response.TokenResponse;
import com.example.concert.waitingQueue.service.WaitingQueueFacade;
import com.example.concert.waitingQueue.service.WaitingQueueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class WaitingQueueController {

    private final WaitingQueueFacade waitingQueueFacade;
    private final WaitingQueueService waitingQueueService;

    public WaitingQueueController(WaitingQueueFacade waitingQueueFacade, WaitingQueueService waitingQueueService){
        this.waitingQueueFacade = waitingQueueFacade;
        this.waitingQueueService = waitingQueueService;
    }

    @PostMapping("/waitingQueue/token")
    public ResponseEntity<TokenResponse> createToken(@RequestBody TokenRequest tokenRequest) throws Exception {
        long concertId = tokenRequest.getConcertId();
        UUID uuid = tokenRequest.getUuid();
        TokenResponse tokenResponse = waitingQueueFacade.createToken(concertId, uuid);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }

    @GetMapping("/waitingQueue/waitingNumber")
    public ResponseEntity<WaitingNumberResponse> retrieveWaitingNumber(@RequestParam(value = "token") String token) throws Exception {
        WaitingNumberResponse waitingNumberResponse = waitingQueueService.getWaitingNumber(token);
        return ResponseEntity.status(HttpStatus.OK).body(waitingNumberResponse);
    }
}
