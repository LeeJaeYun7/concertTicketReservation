package com.example.concert.concert.repository;

import com.example.concert.concert.domain.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConcertRepository extends JpaRepository<Concert, Long> {
    Optional<Concert> findByName(String concertName);
}
