package concerthall.domain;

import global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "concert_hall")
@NoArgsConstructor
public class ConcertHall extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String address;

    private String phoneNumber;

    @Builder
    public ConcertHall(String name, String address, String phoneNumber){
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public static ConcertHall of(String name, String address, String phoneNumber){
        return ConcertHall.builder()
                .name(name)
                .address(address)
                .phoneNumber(phoneNumber)
                .build();
    }
}
