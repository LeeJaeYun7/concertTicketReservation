package seat.presentation;

import org.springframework.web.bind.annotation.RestController;
import seat.application.facade.SeatFacade;

@RestController
public class SeatController {
    private final SeatFacade seatFacade;

    public SeatController(SeatFacade seatFacade){
        this.seatFacade = seatFacade;
    }
}
