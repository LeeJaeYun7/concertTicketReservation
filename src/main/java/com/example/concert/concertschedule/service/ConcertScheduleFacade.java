package com.example.concert.concertschedule.service;

import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.service.SeatService;
import com.example.concert.utils.TokenValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConcertScheduleFacade {

    private final TokenValidator tokenValidator;
    private final ConcertScheduleService concertScheduleService;
    private final SeatService seatService;

    public ConcertScheduleFacade(TokenValidator tokenValidator, ConcertScheduleService concertScheduleService, SeatService seatService){
        this.tokenValidator = tokenValidator;
        this.concertScheduleService = concertScheduleService;
        this.seatService = seatService;
    }

    public List<LocalDateTime> getAvailableDateTimes(String token, long concertId) throws Exception {

        validateToken(token);

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

    public List<Long> getAvailableSeatNumbers(String token, long concertScheduleId) throws Exception {

        validateToken(token);

        List<Seat> availableSeats = seatService.getAllAvailableSeats(concertScheduleId);
        List<Long> availableSeatNumbers = new ArrayList<>();

        for(Seat seat: availableSeats){
            availableSeatNumbers.add(seat.getNumber());
        }

        return availableSeatNumbers;
    }

    private void validateToken(String token) throws Exception {
        boolean isValid = tokenValidator.validateToken(token);

        if(!isValid){
            throw new Exception();
        }
    }
}
