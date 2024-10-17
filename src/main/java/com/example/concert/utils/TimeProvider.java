package com.example.concert.utils;

import java.time.LocalDateTime;

public interface TimeProvider {
    LocalDateTime now();
}
