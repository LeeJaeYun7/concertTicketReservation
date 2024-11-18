package com.example.concert.seatgrade.repository;

import com.example.concert.seatgrade.domain.SeatGrade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatGradeRepository extends JpaRepository<SeatGrade, Long> {
}
