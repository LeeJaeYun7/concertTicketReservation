package com.example.concert.seat.controller;

import com.example.concert.seat.dto.request.SeatReservationRequest;
import com.example.concert.seat.service.SeatFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeatController {

    private final SeatFacade seatFacade;

    public SeatController(SeatFacade seatFacade){
        this.seatFacade = seatFacade;
    }

    @PostMapping("/seat/reservation")
    public ResponseEntity<Void> createSeatReservation(@RequestBody SeatReservationRequest seatReservationRequest) {
        String uuid = seatReservationRequest.getUuid();
        long concertHallId = seatReservationRequest.getConcertHallId();
        long number = seatReservationRequest.getNumber();

        seatFacade.createSeatReservationWithPessimisticLock(uuid, concertHallId, number);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
