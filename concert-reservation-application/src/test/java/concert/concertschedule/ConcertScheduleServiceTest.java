package concert.concertschedule;

import concert.commons.utils.TimeProvider;
import concert.domain.concert.domain.Concert;
import concert.domain.concert.domain.enums.ConcertAgeRestriction;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.concertschedule.application.ConcertScheduleService;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertschedule.domain.ConcertScheduleRepository;
import concert.domain.concertscheduleseat.application.ConcertScheduleSeatService;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeat;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeatRepository;
import concert.domain.concertscheduleseat.domain.enums.SeatStatus;
import concert.domain.seatgrade.domain.SeatGrade;
import concert.domain.seatgrade.domain.enums.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConcertScheduleServiceTest {

  @Mock
  private TimeProvider timeProvider;

  @Mock
  private ConcertScheduleRepository concertScheduleRepository;

  @Mock
  private ConcertScheduleSeatRepository concertScheduleSeatRepository;

  @Mock
  private ConcertScheduleSeatService concertScheduleSeatService;

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

      ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114", null);
      Concert IUConcert = Concert.of("아이유 콘서트", concertHall.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, IUstartAt, IUendAt);

      LocalDateTime IUdateTime = LocalDateTime.of(2024, 10, 18, 22, 30);
      ConcertSchedule IUconcertSchedule = ConcertSchedule.of(IUConcert.getId(), IUdateTime);
      setFieldUsingReflection(IUconcertSchedule, "id", 1L);

      SeatGrade allSeatGrade = SeatGrade.of(IUConcert.getId(), Grade.ALL, 100000);
      ConcertScheduleSeat IUconcertScheduleSeat = ConcertScheduleSeat.of(concertHall.getId(), IUconcertSchedule.getId(), allSeatGrade.getId(), SeatStatus.AVAILABLE);

      when(timeProvider.now()).thenReturn(LocalDateTime.of(2024, 10, 18, 0, 0));

      setFieldUsingReflection(IUconcertScheduleSeat, "id", 1L);
      setFieldUsingReflection(IUconcertScheduleSeat, "updatedAt", timeProvider.now().minusMinutes(10));

      given(concertScheduleRepository.findAllAfterNowByConcertId(1L, timeProvider.now())).willReturn(List.of(IUconcertSchedule));
      given(concertScheduleSeatService.getAllAvailableConcertScheduleSeats(IUconcertSchedule.getId())).willReturn(List.of(IUconcertScheduleSeat));

      List<LocalDateTime> result = sut.getAllAvailableDateTimes(1L);

      assertEquals(1, result.size());
    }
  }

  void setFieldUsingReflection(Object targetObject, String fieldName, Object value) {
    try {
      Class<?> clazz = targetObject.getClass();
      Field field;

      // 상속 계층에서 필드 찾기
      while (clazz != null) {
        try {
          field = clazz.getDeclaredField(fieldName);
          field.setAccessible(true); // 접근 제한 해제
          field.set(targetObject, value); // 필드에 값 설정
          return;
        } catch (NoSuchFieldException e) {
          clazz = clazz.getSuperclass(); // 부모 클래스로 이동
        }
      }

      throw new NoSuchFieldException("필드 " + fieldName + "를 찾을 수 없습니다.");
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("리플렉션으로 필드 값을 설정하는 중 오류가 발생했습니다.", e);
    }
  }

}
