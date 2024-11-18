package com.example.concert.reservation.service;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.reservation.domain.Reservation;
import com.example.concert.reservation.repository.ReservationRepository;
import com.example.concert.seatinfo.domain.SeatInfo;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository){
        this.reservationRepository = reservationRepository;
    }

    public Reservation createReservation(Concert concert, ConcertSchedule concertSchedule, String uuid, SeatInfo seatInfo, long price) {
        reservationRepository.findReservation(concertSchedule.getId(), seatInfo.getId());
        Reservation reservation = Reservation.of(concert, concertSchedule, uuid, seatInfo, price);
        return reservationRepository.save(reservation);
    }
}
