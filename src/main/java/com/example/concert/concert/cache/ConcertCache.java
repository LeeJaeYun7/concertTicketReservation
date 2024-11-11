package com.example.concert.concert.cache;

import com.example.concert.concert.domain.Concert;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface ConcertCache {
    void saveTop30Concerts(List<Concert> concerts) throws JsonProcessingException;
    List<Concert> findTop30Concerts() throws JsonProcessingException;
}
