package concert.domain.concert.services;

import concert.commons.common.CustomException;
import concert.commons.common.ErrorCode;
import concert.commons.common.Loggable;
import concert.domain.concert.entities.SeatGradeEntity;
import concert.domain.concert.entities.dao.SeatGradeEntityDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatGradeService {

    private final SeatGradeEntityDAO seatGradeEntityDAO;

    public long getSeatGradePrice(long seatGradeId){
        SeatGradeEntity seatGrade = seatGradeEntityDAO.findById(seatGradeId).orElseThrow(() -> new CustomException(ErrorCode.SEAT_GRADE_NOT_FOUND, Loggable.ALWAYS));
        return seatGrade.getPrice();
    }
}
