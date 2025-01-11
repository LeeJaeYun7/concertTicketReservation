package concert.application.seatinfo.presentation;

import concert.application.seatinfo.application.facade.SeatInfoFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SeatInfoController {
  private final SeatInfoFacade seatInfoFacade;
}
