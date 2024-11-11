package com.example.concert.concert.service;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.dto.response.ConcertResponse;
import com.example.concert.concert.vo.ConcertVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConcertFacade {

    private final ConcertService concertService;
    public List<ConcertResponse> getTop30ConcertsFromDB() {

        List<Concert> concerts = concertService.getTop30ConcertsFromDB();

        return concerts.stream()
                       .map(ConcertVO::of)
                       .map(ConcertResponse::of)  // Concert 객체를 ConcertResponse로 변환
                       .collect(Collectors.toList());  // 리스트로 수집
    }

    public void saveTop30ConcertsIntoRedis() throws JsonProcessingException {
        concertService.saveTop30ConcertsIntoRedis();
    }

    public List<ConcertResponse> getTop30Concerts() throws JsonProcessingException {
        List<Concert> concerts = concertService.getTop30Concerts();

        return concerts.stream().map(ConcertVO::of)
                                .map(ConcertResponse::of)
                                .toList();
    }
}
