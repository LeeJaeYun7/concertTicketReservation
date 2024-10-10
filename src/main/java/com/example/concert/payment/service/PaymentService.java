package com.example.concert.payment.service;

import com.example.concert.payment.dto.PaymentResponse;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public PaymentResponse createPayment(long uuid, long amount){
        return PaymentResponse.of(true);
    }
}
