package com.example.concert.concertschedule.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.concert.domain.Concert;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.dto.response.ConcertScheduleResponse;
import com.example.concert.concertschedule.repository.ConcertScheduleRepository;
import com.example.concert.concertschedule.vo.ConcertScheduleVO;
import com.example.concert.redis.RedissonDao;
import com.example.concert.utils.TimeProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertScheduleService {

    private final TimeProvider timeProvider;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final RedissonDao redissonDao;

    public void createConcertSchedule(Concert concert, LocalDateTime dateTime, long price) throws JsonProcessingException {
        ConcertSchedule concertSchedule = ConcertSchedule.of(concert, dateTime, price);
        ConcertSchedule savedConcertSchedule = concertScheduleRepository.save(concertSchedule);

        String concertSchedulesJson = redissonDao.getConcertSchedules();
        List<ConcertScheduleVO> concertScheduleVOs = changeConcertScheduleStringtoVO(concertSchedulesJson);
        ConcertScheduleVO concertScheduleVO = ConcertScheduleVO.of(savedConcertSchedule.getConcert().getName(), savedConcertSchedule.getDateTime(), savedConcertSchedule.getPrice());
        concertScheduleVOs.add(concertScheduleVO);

        saveConcertSchedulesIntoRedis(concertScheduleVOs);
    }

    public List<ConcertSchedule> getAllConcertSchedulesAfterNowByConcertId(long concertId){
        LocalDateTime now = timeProvider.now();
        return concertScheduleRepository.findAllAfterNowByConcertId(concertId, now);
    }

    public List<ConcertScheduleResponse> getAllAvailableConcertSchedules() throws JsonProcessingException {
        String concertSchedulesJson = redissonDao.getConcertSchedules();
        List<ConcertScheduleVO> concertScheduleVOs = changeConcertScheduleStringtoVO(concertSchedulesJson);
        return changeConcertScheduleVOtoResponse(concertScheduleVOs);
    }

    public List<ConcertScheduleVO> getAllConcertSchedulesAfterNow(){
        LocalDateTime now = timeProvider.now();
        List<Tuple> concertScheduleTuples = concertScheduleRepository.findAllAfterNow(now);

        return concertScheduleTuples.stream()
                .map(tuple -> ConcertScheduleVO.of(tuple.get(0, String.class),
                                                   tuple.get(1, LocalDateTime.class),
                                                   tuple.get(2, Long.class)))
                .toList();
    }

    public ConcertSchedule getConcertScheduleById(long concertScheduleId) {
        return concertScheduleRepository.findById(concertScheduleId)
                                        .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_SCHEDULE_NOT_FOUND, Loggable.ALWAYS));
    }

    public void saveConcertSchedulesIntoRedis(List<ConcertScheduleVO> concertScheduleVOs){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            String concertSchedulesJson = objectMapper.writeValueAsString(concertScheduleVOs);
            redissonDao.saveConcertSchedules(concertSchedulesJson);
        } catch (Exception e) {
            log.error("Concert Schedule failed to save into redis cache", e);
        }
    }

    public List<ConcertScheduleVO> changeConcertScheduleStringtoVO(String concertSchedulesJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper.readValue(concertSchedulesJson, objectMapper.getTypeFactory().constructCollectionType(List.class, ConcertScheduleVO.class));
    }

    public List<ConcertScheduleResponse> changeConcertScheduleVOtoResponse(List<ConcertScheduleVO> concertScheduleVOs){
        List<ConcertScheduleResponse> concertSchedules = new ArrayList<>();

        for(ConcertScheduleVO concertScheduleVO: concertScheduleVOs){
            ConcertScheduleResponse concertScheduleResponse = ConcertScheduleResponse.of(concertScheduleVO.getConcertName(), concertScheduleVO.getDateTime(), concertScheduleVO.getPrice());
            concertSchedules.add(concertScheduleResponse);
        }

        return concertSchedules;
    }
}
