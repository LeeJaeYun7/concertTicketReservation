package com.example.concert.concertschedule.service;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.service.ConcertService;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.dto.response.ConcertScheduleResponse;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.service.SeatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertScheduleFacade {

    private final ConcertService concertService;
    private final ConcertScheduleService concertScheduleService;
    private final SeatService seatService;

    public void createConcertSchedule(String concertName, LocalDateTime dateTime, long price) {
        Concert concert = concertService.getConcertByName(concertName);
        concertScheduleService.createConcertSchedule(concert, dateTime, price);
    }
    public List<LocalDateTime> getAvailableDateTimes(long concertId) {

        List<ConcertSchedule> allConcertSchedules = concertScheduleService.getAllConcertSchedulesAfterNowByConcertId(concertId);
        List<LocalDateTime> availableDateTimes = new ArrayList<>();

        for(ConcertSchedule concertSchedule: allConcertSchedules){
            long concertScheduleId = concertSchedule.getId();

            List<Seat> availableSeats = seatService.getAllAvailableSeats(concertScheduleId);

            if(!availableSeats.isEmpty()){
                availableDateTimes.add(concertSchedule.getDateTime());
            }
        }
        return availableDateTimes;
    }

    public List<Long> getAvailableSeatNumbers(long concertScheduleId) {

        List<Seat> availableSeats = seatService.getAllAvailableSeats(concertScheduleId);
        List<Long> availableSeatNumbers = new ArrayList<>();

        for(Seat seat: availableSeats){
            availableSeatNumbers.add(seat.getNumber());
        }

        return availableSeatNumbers;
    }
}
