package com.example.concert.token.dto;

import lombok.Getter;

@Getter
public class TokenRequest {

    private final long uuid;

    public TokenRequest(long uuid){
        this.uuid = uuid;
    }
}
