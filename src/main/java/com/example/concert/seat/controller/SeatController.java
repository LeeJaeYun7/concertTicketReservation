package com.example.concert.seat.controller;

import com.example.concert.seat.service.SeatFacade;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeatController {
    private final SeatFacade seatFacade;

    public SeatController(SeatFacade seatFacade){
        this.seatFacade = seatFacade;
    }
}
