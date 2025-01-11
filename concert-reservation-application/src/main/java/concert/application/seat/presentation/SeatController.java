package concert.application.seat.presentation;

import concert.application.seat.application.facade.SeatFacade;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeatController {
  private final SeatFacade seatFacade;

  public SeatController(SeatFacade seatFacade) {
    this.seatFacade = seatFacade;
  }
}
