package com.example.concert.concertschedule.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.dto.response.AvailableDateTimesResponse;
import com.example.concert.concertschedule.dto.response.ConcertScheduleResponse;
import com.example.concert.concertschedule.repository.ConcertScheduleRepository;
import com.example.concert.concertschedule.vo.ConcertScheduleVO;
import com.example.concert.utils.TimeProvider;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.asm.Advice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConcertScheduleService {

    private final TimeProvider timeProvider;
    private final ConcertScheduleRepository concertScheduleRepository;

    public List<ConcertSchedule> getAllConcertSchedulesAfterNowByConcertId(long concertId){
        LocalDateTime now = timeProvider.now();
        return concertScheduleRepository.findAllAfterNowByConcertId(concertId, now);
    }

    public List<ConcertScheduleResponse> getAllAvailableConcertSchedules(){
        LocalDateTime now = timeProvider.now();
        List<Tuple> concertScheduleTuples = concertScheduleRepository.findAllAfterNow(now);
        List<ConcertScheduleVO> concertScheduleVOs = concertScheduleTuples.stream()
                .map(tuple -> new ConcertScheduleVO(tuple.get(0, String.class),
                                                    tuple.get(1, LocalDateTime.class),
                                                    tuple.get(2, Long.class)))
                                                    .toList();

        List<ConcertScheduleResponse> concertSchedules = new ArrayList<>();

        for(ConcertScheduleVO concertScheduleVO: concertScheduleVOs){
            ConcertScheduleResponse concertScheduleResponse = ConcertScheduleResponse.of(concertScheduleVO.getConcertName(), concertScheduleVO.getDateTime(), concertScheduleVO.getPrice());
            concertSchedules.add(concertScheduleResponse);
        }

        return concertSchedules;
    }

    public ConcertSchedule getConcertScheduleById(long concertScheduleId) {
        return concertScheduleRepository.findById(concertScheduleId)
                                        .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_SCHEDULE_NOT_FOUND, Loggable.ALWAYS));
    }
}
