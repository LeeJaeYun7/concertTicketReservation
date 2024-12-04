package com.example.concert.seatinfo.controller;

import com.example.concert.seatinfo.dto.response.SeatInfoRequest;
import com.example.concert.seatinfo.service.SeatInfoFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SeatInfoController {

    private final SeatInfoFacade seatInfoFacade;

    @PostMapping("/api/v1/seatInfo")
    public void createSeatInfoReservation(@RequestBody SeatInfoRequest seatInfoRequest) {
        String uuid = seatInfoRequest.getUuid();
        long concertScheduleId = seatInfoRequest.getConcertScheduleId();
        long seatNumber = seatInfoRequest.getSeatNumber();

        seatInfoFacade.createSeatInfoReservationWithDistributedLock(uuid, concertScheduleId, seatNumber);
    }
}
