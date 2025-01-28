package concert.domain.member.entities;

import concert.domain.shared.entities.BaseTimeEntity;
import concert.domain.shared.utils.SnowFlakeGenerator;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor
public class MemberEntity extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(unique = true, nullable = false)
  private String uuid;

  private String name;

  private long balance;

  @Builder
  public MemberEntity(String uuid, String name, long balance) {
    this.uuid = uuid;
    this.name = name;
    this.balance = balance;
    this.setCreatedAt(LocalDateTime.now());
    this.setUpdatedAt(LocalDateTime.now());
  }

  public static MemberEntity of(String name) {
    String uuid = SnowFlakeGenerator.createSnowFlake();

    return MemberEntity.builder()
                        .uuid(uuid)
                        .name(name)
                        .balance(0)
                        .build();
  }
}

