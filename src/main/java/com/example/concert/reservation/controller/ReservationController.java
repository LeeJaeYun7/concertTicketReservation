package com.example.concert.reservation.controller;

import com.example.concert.concert.dto.response.ConcertResponse;
import com.example.concert.reservation.dto.request.ReservationRequest;
import com.example.concert.reservation.dto.response.ReservationResponse;
import com.example.concert.reservation.service.ReservationFacade;
import com.example.concert.reservation.vo.ReservationVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
public class ReservationController {

    private final ReservationFacade reservationFacade;

    public ReservationController(ReservationFacade reservationFacade){
        this.reservationFacade = reservationFacade;
    }

    @PostMapping("/reservation")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest reservationRequest, HttpServletRequest request) throws ExecutionException, InterruptedException {
        String uuid = reservationRequest.getUuid();
        long concertScheduleId = reservationRequest.getConcertScheduleId();
        long seatNumber = reservationRequest.getSeatNumber();

        CompletableFuture<ReservationVO> reservationVO = reservationFacade.createReservation(uuid, concertScheduleId, seatNumber);
        ReservationResponse reservationResponse = ReservationResponse.of(reservationVO.get().getName(), reservationVO.get().getConcertName(), reservationVO.get().getDateTime(), reservationVO.get().getPrice());

        return ResponseEntity.status(HttpStatus.OK).body(reservationResponse);
    }
}
