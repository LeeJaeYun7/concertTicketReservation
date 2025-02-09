package concert.domain.concerthall;

import concert.domain.concerthall.entities.ConcertHallEntity;
import concert.domain.concerthall.entities.ConcertHallSeatEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Accessors(chain = true)
@Slf4j
public class ConcertHall {
    private ConcertHallEntity concertHallEntity;
    private ConcertHallSeatEntity concertHallSeatEntity;
}
