package com.example.concert.concerthall.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.concerthall.domain.ConcertHall;
import com.example.concert.concerthall.repository.ConcertHallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcertHallService {

     private final ConcertHallRepository concertHallRepository;
     public ConcertHall getConcertHallById(long concertHallId){
         return concertHallRepository.findById(concertHallId).orElseThrow(() -> new CustomException(ErrorCode.CONCERT_HALL_NOT_FOUND, Loggable.ALWAYS));
     }
}
