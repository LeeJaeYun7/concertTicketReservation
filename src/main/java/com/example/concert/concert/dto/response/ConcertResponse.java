package com.example.concert.concert.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConcertResponse {

    private String name;

    @Builder
    public ConcertResponse(String name){
        this.name = name;
    }

    public static ConcertResponse of(String name){
        return ConcertResponse.builder()
                .name(name)
                .build();
    }
}
