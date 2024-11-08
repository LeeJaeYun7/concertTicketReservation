package com.example.concert.concert.controller;

import com.example.concert.concert.dto.response.ConcertResponse;
import com.example.concert.concert.service.ConcertFacade;
import com.example.concert.concert.service.ConcertService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertFacade concertFacade;
    private final ConcertService concertService;

    @GetMapping("/concert/redis")
    public ResponseEntity<List<ConcertResponse>> retrieveAllConcertsFromRedis() throws JsonProcessingException {
        List<ConcertResponse> concertResponses = concertFacade.getAllConcertsFromRedis();
        return ResponseEntity.status(HttpStatus.OK).body(concertResponses);
    }

    @GetMapping("/concert/redis/init")
    public ResponseEntity<Void> saveAllConcertsToRedis() throws JsonProcessingException {
        concertService.saveAllConcertsToRedis();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/concert/db")
    public ResponseEntity<List<ConcertResponse>> retrieveAllConcertsFromDB() throws JsonProcessingException {
        List<ConcertResponse> concertResponses = concertFacade.getAllConcertsFromDB();
        return ResponseEntity.status(HttpStatus.OK).body(concertResponses);
    }
}
