package com.example.concert.balance.service;

import com.example.concert.balance.dto.ChargeResponse;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {

    public ChargeResponse chargeBalance(long uuid, long amount){
        return ChargeResponse.of(true);
    }
}
