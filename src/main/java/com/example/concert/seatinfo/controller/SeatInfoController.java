package com.example.concert.seatinfo.controller;

import com.example.concert.seatinfo.service.SeatInfoFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SeatInfoController {
    private final SeatInfoFacade seatInfoFacade;
}
