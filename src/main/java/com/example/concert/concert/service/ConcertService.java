package com.example.concert.concert.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.repository.ConcertRepository;
import com.example.concert.concert.vo.ConcertVO;
import com.example.concert.redis.ConcertDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertDao concertDao;

    public List<ConcertVO> getAllConcertsFromRedis() throws JsonProcessingException {
        List<Concert> concerts = concertDao.getConcerts();
        return changeConcertEntityToVO(concerts);
    }

    public List<ConcertVO> getAllConcertsFromDB() {
        List<Concert> concerts = concertRepository.findAll();
        return concerts.stream().map(ConcertVO::of).toList();
    }

    public Concert getConcertById(long concertId) {
        return concertRepository.findById(concertId)
                                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND, Loggable.ALWAYS));
    }

    public Concert getConcertByName(String concertName) {
        return concertRepository.findByName(concertName)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND, Loggable.ALWAYS));
    }

    public List<Long> getAllConcertIds() {
        return concertRepository.findAll().stream().map(Concert::getId).collect(Collectors.toList());
    }

    public List<ConcertVO> changeConcertEntityToVO(List<Concert> concerts) {
        return concerts.stream()
                .map(ConcertVO::of)  // Concert 객체를 ConcertVO 객체로 변환
                .collect(Collectors.toList());  // 변환된 객체들을 리스트로 수집
    }

    public void saveAllConcertsToRedis() throws JsonProcessingException {
        List<Concert> concerts = concertRepository.findAll();
        concertDao.saveConcerts(concerts);
    }
}
