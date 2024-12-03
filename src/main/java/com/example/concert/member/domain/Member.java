package com.example.concert.member.domain;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.global.entity.BaseTimeEntity;
import com.example.concert.utils.SnowFlakeGenerator;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private String uuid;

    private String name;

    private long balance;

    @Builder
    public Member(String uuid, String name, long balance){
        this.uuid = uuid;
        this.name = name;
        this.balance = balance;
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
    }

    public static Member of(String name) {
        String uuid = SnowFlakeGenerator.createSnowFlake();

        return Member.builder()
                     .uuid(uuid)
                     .name(name)
                     .balance(0)
                     .build();
    }
    public void updateBalance(long balance) {
        if (balance < 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE, Loggable.ALWAYS);
        }
        this.balance = balance;
    }
}

