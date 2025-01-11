package concert.concertschedule;

import concert.commons.utils.TimeProvider;
import concert.domain.concert.domain.Concert;
import concert.domain.concert.domain.enums.ConcertAgeRestriction;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.concertschedule.application.ConcertScheduleService;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertschedule.domain.ConcertScheduleRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Disabled
public class ConcertScheduleServiceTest {

  @Mock
  private TimeProvider timeProvider;

  @Mock
  private ConcertScheduleRepository concertScheduleRepository;

  @InjectMocks
  private ConcertScheduleService sut;


  @Nested
  @DisplayName("현재 이후의 모든 콘서트 스케줄을 가져올 때")
  class 현재_이후의_모든_콘서트_스케줄을_가져올때 {
    @Test
    @DisplayName("성공한다")
    void 성공한다() {
      LocalDate IUstartAt = LocalDate.of(2024, 10, 16);
      LocalDate IUendAt = LocalDate.of(2024, 10, 18);

      ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
      Concert IUConcert = Concert.of("아이유 콘서트", concertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, IUstartAt, IUendAt);

      LocalDateTime IUdateTime = LocalDateTime.of(2024, 10, 18, 22, 30);
      ConcertSchedule IUconcertSchedule = ConcertSchedule.of(IUConcert, IUdateTime);

      when(timeProvider.now()).thenReturn(LocalDateTime.of(2024, 10, 18, 0, 0));

      given(concertScheduleRepository.findAllAfterNowByConcertId(1L, timeProvider.now())).willReturn(List.of(IUconcertSchedule));

      List<LocalDateTime> result = sut.getAllAvailableDateTimes(1L);

      assertEquals(1, result.size());
    }
  }
}
