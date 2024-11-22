package com.example.concert.reservation.infrastructure.messaging;

import com.example.concert.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Entity
@Getter
@Table(name = "outbox")
public class Outbox extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender")
    private String sender;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "subject")
    private String subject;

    @Column(name = "message")
    private String message;

    private boolean sent;

    @Builder
    public Outbox(String sender, String recipient, String subject, String message, boolean sent) {
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.sent = sent;
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
    }

    public static Outbox of(String sender, String recipient, String subject, String message, boolean sent){
        return Outbox.builder()
                .sender(sender)
                .recipient(recipient)
                .subject(subject)
                .message(message)
                .sent(sent)
                .build();
    }

    public void updateSent(boolean sent){
        this.sent = sent;
    }
}
