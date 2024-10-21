package com.example.concert.concertschedule.controller;

import com.example.concert.concertschedule.dto.request.ConcertScheduleRequest;
import com.example.concert.concertschedule.dto.request.SeatNumbersRequest;
import com.example.concert.concertschedule.dto.response.ConcertScheduleResponse;
import com.example.concert.concertschedule.service.ConcertScheduleFacade;
import com.example.concert.seat.dto.response.SeatNumbersResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConcertScheduleController {

    private final ConcertScheduleFacade concertScheduleFacade;

    public ConcertScheduleController(ConcertScheduleFacade concertScheduleFacade){
        this.concertScheduleFacade = concertScheduleFacade;
    }

    @GetMapping("/concertSchedule")
    public ResponseEntity<ConcertScheduleResponse> retrieveAvailableDateTimes(@RequestBody ConcertScheduleRequest concertScheduleRequest) throws Exception {

        String token = concertScheduleRequest.getToken();
        long concertId = concertScheduleRequest.getConcertId();

        ConcertScheduleResponse concertScheduleResponse = concertScheduleFacade.getAvailableDateTimes(token, concertId);
        return ResponseEntity.status(HttpStatus.OK).body(concertScheduleResponse);
    }

    @GetMapping("/concertSchedule/seats")
    public ResponseEntity<SeatNumbersResponse> retrieveAvailableSeats(@RequestBody SeatNumbersRequest seatNumbersRequest) throws Exception {

        String token = seatNumbersRequest.getToken();
        long concertScheduleId = seatNumbersRequest.getConcertScheduleId();

        SeatNumbersResponse seatsResponse = concertScheduleFacade.getAvailableSeatNumbers(token, concertScheduleId);
        return ResponseEntity.status(HttpStatus.OK).body(seatsResponse);
    }
}
