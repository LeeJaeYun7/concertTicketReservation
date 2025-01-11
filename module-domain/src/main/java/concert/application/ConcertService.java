package concert.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import common.CustomException;
import common.ErrorCode;
import common.Loggable;
import concert.cache.ConcertCache;
import concert.domain.Concert;
import concert.domain.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reservation.domain.ReservationRepository;
import utils.TimeProvider;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final TimeProvider timeProvider;
    private final ConcertRepository concertRepository;
    private final ReservationRepository reservationRepository;
    private final ConcertCache concertCache;

    public Concert getConcertById(long concertId) {
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND, Loggable.ALWAYS));
    }

    public Concert getConcertByName(String concertName) {
        return concertRepository.findByName(concertName)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND, Loggable.ALWAYS));
    }

    public List<Long> getAllConcertIds() {
        return concertRepository.findAll().stream().map(Concert::getId).collect(Collectors.toList());
    }

    @Transactional
    public void saveTop30ConcertsIntoRedis() throws JsonProcessingException {
        LocalDateTime now = timeProvider.now();
        LocalDateTime threeDaysAgo = now.minus(Duration.ofHours(72));

        List<Concert> top30concerts = reservationRepository.findTop30Concerts(threeDaysAgo);
        concertCache.saveTop30Concerts(top30concerts);
    }

    @Transactional
    public List<Concert> getTop30ConcertsFromDB() {
        LocalDateTime now = timeProvider.now();
        LocalDateTime threeDaysAgo = now.minus(Duration.ofHours(72));

        return reservationRepository.findTop30Concerts(threeDaysAgo);
    }

    public List<Concert> getTop30Concerts() throws JsonProcessingException {

        if(concertCache.findTop30Concerts() != null){
            return concertCache.findTop30Concerts();
        }

        LocalDateTime now = timeProvider.now();
        LocalDateTime threeDaysAgo = now.minus(Duration.ofHours(72));

        return reservationRepository.findTop30Concerts(threeDaysAgo);
    }
}
