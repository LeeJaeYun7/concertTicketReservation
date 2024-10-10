package com.example.concert.member.entity;

import com.example.concert.global.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;

@Entity
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long uuid;

    private String name;

    private long balance;

    @Builder
    public Member(long uuid, String name, long balance){
        this.uuid = uuid;
        this.name = name;
        this.balance = balance;
    }
}

