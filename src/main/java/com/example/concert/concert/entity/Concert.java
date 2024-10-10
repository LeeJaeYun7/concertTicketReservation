package com.example.concert.concert.entity;

import com.example.concert.global.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;

@Entity
public class Concert extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final long id;

    private final String name;

    @Builder
    public Concert(long id, String name){
        this.id = id;
        this.name = name;
    }
}
