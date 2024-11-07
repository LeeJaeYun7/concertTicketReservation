package com.example.concert.concert.controller;

import com.example.concert.concert.dto.response.ConcertResponse;
import com.example.concert.concert.service.ConcertFacade;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertFacade concertFacade;
    @Timed(value = "api.concert.requests", extraTags = {"method", "GET"})
    @GetMapping("/concert")
    public ResponseEntity<ConcertResponse> retrieveConcert(@RequestParam(value="concertId") long concertId){
        ConcertResponse concertResponse = concertFacade.getConcertById(concertId);
        return ResponseEntity.status(HttpStatus.CREATED).body(concertResponse);
    }
}
