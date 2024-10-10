package com.example.concert.balance.entity;

import com.example.concert.global.entity.BaseTimeEntity;
import com.example.concert.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;

@Entity
public class ChargeHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Member member;

    private long amount;

    @Builder
    public ChargeHistory(long id, Member member, long amount){
        this.id = id;
        this.member = member;
        this.amount = amount;
    }
}
