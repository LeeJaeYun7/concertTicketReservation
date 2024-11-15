package com.example.concert.seatassignment.domain;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concertgrade.domain.ConcertGrade;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.seat.domain.Seat;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "seat_assignment")
@NoArgsConstructor
public class SeatAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id")
    private Concert concert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_grade_id")
    private ConcertGrade concertGrade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_schedule_id")
    private ConcertSchedule concertSchedule;

    @Builder
    public SeatAssignment(Seat seat, Concert concert, ConcertGrade concertGrade, ConcertSchedule concertSchedule) {
        this.seat = seat;
        this.concert = concert;
        this.concertGrade = concertGrade;
        this.concertSchedule = concertSchedule;
    }
}