package com.example.concert.payment.service;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.payment.domain.Payment;
import com.example.concert.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository){
        this.paymentRepository = paymentRepository;
    }

    public void createPayment(Concert concert, ConcertSchedule concertSchedule, String uuid, long amount){
        Payment payment = Payment.of(concert, concertSchedule, uuid, amount);
        paymentRepository.save(payment);
    }
}
