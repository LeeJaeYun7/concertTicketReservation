package com.example.concert.balance.controller;

import com.example.concert.balance.dto.ChargeRequest;
import com.example.concert.balance.dto.ChargeResponse;
import com.example.concert.balance.service.BalanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService){
        this.balanceService = balanceService;
    }

    @PostMapping("/balance/charge")
    public ResponseEntity<ChargeResponse> chargeBalance(@RequestBody ChargeRequest chargeRequest){
        long uuid = chargeRequest.getUuid();
        long amount = chargeRequest.getAmount();
        ChargeResponse chargeResponse = balanceService.chargeBalance(uuid, amount);
        return ResponseEntity.status(HttpStatus.OK).body(chargeResponse);
    }
}
