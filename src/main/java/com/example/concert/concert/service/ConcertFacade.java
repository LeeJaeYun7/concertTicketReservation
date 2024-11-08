package com.example.concert.concert.service;

import com.example.concert.concert.dto.response.ConcertResponse;
import com.example.concert.concert.vo.ConcertVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConcertFacade {

    private final ConcertService concertService;


    public List<ConcertResponse> getAllConcertsFromRedis() throws JsonProcessingException {

        List<ConcertVO> concertVOs = concertService.getAllConcertsFromRedis();

        return concertVOs.stream()
                         .map(ConcertResponse::of)  // Concert 객체를 ConcertResponse로 변환
                         .collect(Collectors.toList());  // 리스트로 수집
    }
    public List<ConcertResponse> getAllConcertsFromDB() throws JsonProcessingException {

        List<ConcertVO> concertVOs = concertService.getAllConcertsFromDB();

        return concertVOs.stream()
                         .map(ConcertResponse::of)  // Concert 객체를 ConcertResponse로 변환
                         .collect(Collectors.toList());  // 리스트로 수집
    }
}
