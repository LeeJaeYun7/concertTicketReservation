package seatinfo.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import seatinfo.application.facade.SeatInfoFacade;

@RestController
@RequiredArgsConstructor
public class SeatInfoController {
    private final SeatInfoFacade seatInfoFacade;
}
