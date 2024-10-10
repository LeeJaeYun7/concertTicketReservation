package com.example.concert.reservation.service;

import com.example.concert.reservation.dto.ReservationResponse;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    public ReservationResponse createReservation(long uuid, long seatId, long price) {
        return ReservationResponse.of(true);
    }
}
