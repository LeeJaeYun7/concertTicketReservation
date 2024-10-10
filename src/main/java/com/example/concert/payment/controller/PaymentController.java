package com.example.concert.payment.controller;

import com.example.concert.payment.dto.PaymentRequest;
import com.example.concert.payment.dto.PaymentResponse;
import com.example.concert.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService){
        this.paymentService = paymentService;
    }

    @PostMapping("/payment/create")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest paymentRequest){
        long uuid = paymentRequest.getUuid();
        long amount = paymentRequest.getAmount();

        PaymentResponse paymentResponse = paymentService.createPayment(uuid, amount);
        return ResponseEntity.status(HttpStatus.OK).body(paymentResponse);
    }
}
