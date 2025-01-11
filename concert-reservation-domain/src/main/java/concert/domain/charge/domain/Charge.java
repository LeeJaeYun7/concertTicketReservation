package concert.domain.charge.domain;

import concert.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "charge")
public class Charge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String uuid;

    private long amount;

    @Builder
    public Charge(String uuid, long amount){
        this.uuid = uuid;
        this.amount = amount;
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
    }

    public static Charge of(String uuid, long amount){

        return Charge.builder()
                .uuid(uuid)
                .amount(amount)
                .build();
    }
}
