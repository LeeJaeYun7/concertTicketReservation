package concert.domain.concert.services;

import concert.domain.concert.entities.ConcertSeatGradeEntity;
import concert.domain.concert.entities.dao.SeatGradeEntityDAO;
import concert.domain.concert.exceptions.ConcertException;
import concert.domain.concert.exceptions.ConcertExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatGradeService {

    private final SeatGradeEntityDAO seatGradeEntityDAO;

    public long getSeatGradePrice(long seatGradeId){
        ConcertSeatGradeEntity seatGrade = seatGradeEntityDAO.findById(seatGradeId).orElseThrow(() -> new ConcertException(ConcertExceptionType.SEAT_GRADE_NOT_FOUND));
        return seatGrade.getPrice();
    }
}
