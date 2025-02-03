package concert.domain.concert;

import concert.domain.concert.entities.ConcertEntity;
import concert.domain.concert.entities.ConcertScheduleEntity;
import concert.domain.concert.entities.ConcertScheduleSeatEntity;
import concert.domain.concert.entities.ConcertSeatGradeEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Getter
@Setter
@Accessors(chain = true)
@Slf4j
public class Concert {

    private ConcertEntity concertEntity;
    private List<ConcertScheduleEntity> concertScheduleEntities;
    private List<ConcertScheduleSeatEntity> concertScheduleSeatEntities;
    private List<ConcertSeatGradeEntity> concertSeatGradeEntity;
}
