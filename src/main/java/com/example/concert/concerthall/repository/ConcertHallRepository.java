package com.example.concert.concerthall.repository;

import com.example.concert.concerthall.domain.ConcertHall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertHallRepository extends JpaRepository<ConcertHall, Long> {
}
