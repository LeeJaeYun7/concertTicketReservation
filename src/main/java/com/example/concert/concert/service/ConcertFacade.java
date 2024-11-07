package com.example.concert.concert.service;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.dto.response.ConcertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcertFacade {

    private final ConcertService concertService;

    public ConcertResponse getConcertById(long concertId){
        Concert concert = concertService.getConcertById(concertId);
        return ConcertResponse.of(concert.getName());
    }
}
