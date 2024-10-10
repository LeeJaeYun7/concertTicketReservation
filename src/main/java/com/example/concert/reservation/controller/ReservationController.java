package com.example.concert.reservation.controller;

import com.example.concert.reservation.dto.ReservationRequest;
import com.example.concert.reservation.dto.ReservationResponse;
import com.example.concert.reservation.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService){
        this.reservationService = reservationService;
    }

    @PostMapping("/reservation/create")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest reservationRequest){
        long uuid = reservationRequest.getUuid();
        long seatId = reservationRequest.getSeatId();
        long price = reservationRequest.getPrice();

        ReservationResponse reservationResponse = reservationService.createReservation(uuid, seatId, price);

        return ResponseEntity.status(HttpStatus.OK).body(reservationResponse);
    }
}
