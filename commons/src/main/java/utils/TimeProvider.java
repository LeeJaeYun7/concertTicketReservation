package utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public interface TimeProvider {
    LocalDateTime now();
}
