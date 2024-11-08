package com.example.concert.concert.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.repository.ConcertRepository;
import com.example.concert.concert.vo.ConcertVO;
import com.example.concert.redis.ConcertDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
        String concertsJson = concertDao.getConcerts();
        return changeConcertStringtoVO(concertsJson);
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

    public List<ConcertVO> changeConcertStringtoVO(String concertJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(concertJson, objectMapper.getTypeFactory().constructCollectionType(List.class, ConcertVO.class));
    }

    public void saveAllConcertsToRedis() throws JsonProcessingException {
        List<Concert> concerts = concertRepository.findAll();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        String concertsJson = objectMapper.writeValueAsString(concerts);
        concertDao.saveConcerts(concertsJson);
    }
}
