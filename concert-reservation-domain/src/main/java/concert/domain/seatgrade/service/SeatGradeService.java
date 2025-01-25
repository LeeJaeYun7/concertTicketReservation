package concert.domain.seatgrade.service;

import concert.commons.common.CustomException;
import concert.commons.common.ErrorCode;
import concert.commons.common.Loggable;
import concert.domain.seatgrade.domain.SeatGrade;
import concert.domain.seatgrade.domain.SeatGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatGradeService {

    private final SeatGradeRepository seatGradeRepository;

    public long getSeatGradePrice(long seatGradeId){
        SeatGrade seatGrade = seatGradeRepository.findById(seatGradeId).orElseThrow(() -> new CustomException(ErrorCode.SEAT_GRADE_NOT_FOUND, Loggable.ALWAYS));
        return seatGrade.getPrice();
    }
}
