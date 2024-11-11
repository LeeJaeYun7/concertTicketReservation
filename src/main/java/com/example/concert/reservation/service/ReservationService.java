package com.example.concert.reservation.service;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.reservation.domain.Reservation;
import com.example.concert.reservation.repository.ReservationRepository;
import com.example.concert.seat.domain.Seat;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository){
        this.reservationRepository = reservationRepository;
    }

    public Reservation createReservation(Concert concert, ConcertSchedule concertSchedule, String uuid, Seat seat, long price) {
        System.out.println("여기 도착1");

        reservationRepository.findReservationByConcertScheduleIdAndSeatId(concertSchedule.getId(), seat.getId());
        System.out.println("여기 도착2");
        Reservation reservation = Reservation.of(concert, concertSchedule, uuid, seat, price);
        System.out.println("여기 도착3");
        return reservationRepository.save(reservation);
    }
}
