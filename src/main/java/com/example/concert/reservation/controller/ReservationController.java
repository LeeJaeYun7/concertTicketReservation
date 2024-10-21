package com.example.concert.reservation.controller;

import com.example.concert.reservation.dto.ReservationRequest;
import com.example.concert.reservation.dto.ReservationResponse;
import com.example.concert.reservation.service.ReservationFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ReservationController {

    private final ReservationFacade reservationFacade;

    public ReservationController(ReservationFacade reservationFacade){
        this.reservationFacade = reservationFacade;
    }

    @PostMapping("/reservation")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest reservationRequest) throws Exception {
        String token = reservationRequest.getToken();
        UUID uuid = reservationRequest.getUuid();
        long concertScheduleId = reservationRequest.getConcertScheduleId();
        long seatNumber = reservationRequest.getSeatNumber();

        ReservationResponse reservationResponse = reservationFacade.createReservation(token, uuid, concertScheduleId, seatNumber);
        return ResponseEntity.status(HttpStatus.OK).body(reservationResponse);
    }
}
