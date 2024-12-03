package com.example.concert.factory;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.enums.ConcertAgeRestriction;
import com.example.concert.concert.repository.ConcertRepository;
import com.example.concert.concerthall.domain.ConcertHall;
import com.example.concert.concerthall.repository.ConcertHallRepository;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.repository.ConcertScheduleRepository;
import com.example.concert.member.domain.Member;
import com.example.concert.member.repository.MemberRepository;
import com.example.concert.reservation.infrastructure.repository.ReservationRepository;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.repository.SeatRepository;
import com.example.concert.seatgrade.domain.SeatGrade;
import com.example.concert.seatgrade.enums.Grade;
import com.example.concert.seatgrade.repository.SeatGradeRepository;
import com.example.concert.seatinfo.domain.SeatInfo;
import com.example.concert.seatinfo.enums.SeatStatus;
import com.example.concert.seatinfo.repository.SeatInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
@Component
public class TestDataFactory {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertHallRepository concertHallRepository;
    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;
    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatGradeRepository seatGradeRepository;

    @Autowired
    private SeatInfoRepository seatInfoRepository;

    @Autowired
    private ReservationRepository reservationRepository;


    public Member createMember() {
        Member member = Member.of("Tom Cruise");
        member.updateBalance(100000);
        return memberRepository.save(member);
    }

    public ConcertHall createConcertHall() {
        ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
        return concertHallRepository.save(concertHall);
    }

    public Concert createConcert(ConcertHall concertHall) {
        LocalDate startAt = LocalDate.of(2024, 11, 25);
        LocalDate endAt = LocalDate.of(2024, 11, 28);
        Concert concert = Concert.of("브루노마스 콘서트", concertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);
        return concertRepository.save(concert);
    }

    public ConcertSchedule createConcertSchedule(Concert concert) {
        LocalDateTime dateTime = LocalDateTime.of(2024, 11, 25, 22, 30);
        return concertScheduleRepository.save(ConcertSchedule.of(concert, dateTime));
    }

    public Seat createSeat(ConcertHall concertHall) {
        Seat seat = Seat.of(concertHall, 1);
        return seatRepository.save(seat);
    }

    public SeatGrade createSeatGrade(Concert concert) {
        SeatGrade vipSeatGrade = SeatGrade.of(concert, Grade.VIP, 100000);
        return seatGradeRepository.save(vipSeatGrade);
    }

    public SeatInfo createSeatInfo(Seat seat, ConcertSchedule concertSchedule, SeatGrade seatGrade) {
        SeatInfo vipSeatInfo = SeatInfo.of(seat, concertSchedule, seatGrade, SeatStatus.AVAILABLE);
        return seatInfoRepository.save(vipSeatInfo);
    }
}
