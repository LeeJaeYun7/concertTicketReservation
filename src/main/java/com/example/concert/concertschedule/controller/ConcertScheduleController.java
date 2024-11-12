package com.example.concert.concertschedule.controller;

import com.example.concert.concertschedule.dto.request.ConcertScheduleCreateRequest;
import com.example.concert.concertschedule.dto.response.AvailableDateTimesResponse;
import com.example.concert.concertschedule.service.ConcertScheduleFacade;
import com.example.concert.seat.dto.response.SeatNumbersResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConcertScheduleController {

    private final ConcertScheduleFacade concertScheduleFacade;

    @PostMapping("/concertSchedule")
    public ResponseEntity<Void> createConcertSchedule(@RequestBody ConcertScheduleCreateRequest concertScheduleCreateRequest) throws JsonProcessingException {
        String concertName = concertScheduleCreateRequest.getConcertName();
        LocalDateTime dateTime = concertScheduleCreateRequest.getDateTime();
        long price = concertScheduleCreateRequest.getPrice();

        concertScheduleFacade.createConcertSchedule(concertName, dateTime, price);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/concertSchedule")
    public ResponseEntity<AvailableDateTimesResponse> retrieveAvailableDateTimes(@RequestParam long concertId) {
        List<LocalDateTime> availableDateTimes = concertScheduleFacade.getAvailableDateTimes(concertId);
        AvailableDateTimesResponse availableDateTimesResponse = AvailableDateTimesResponse.of(availableDateTimes);

        return ResponseEntity.status(HttpStatus.OK).body(availableDateTimesResponse);
    }

    @GetMapping("/concertSchedule/seats")
    public ResponseEntity<SeatNumbersResponse> retrieveAvailableSeats(@RequestParam long concertScheduleId) {
        List<Long> availableSeatNumbers = concertScheduleFacade.getAvailableSeatNumbers(concertScheduleId);
        SeatNumbersResponse seatsResponse = SeatNumbersResponse.of(availableSeatNumbers);

        return ResponseEntity.status(HttpStatus.OK).body(seatsResponse);
    }
}
