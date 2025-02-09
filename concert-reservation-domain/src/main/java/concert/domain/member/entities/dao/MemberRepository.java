package concert.domain.member.entities.dao;

import concert.domain.member.entities.MemberEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findByUuid(String uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m from MemberEntity m WHERE m.uuid = :uuid")
    Optional<MemberEntity> findByUuidWithLock(@Param("uuid") String uuid);

    @Query("SELECT m FROM MemberEntity m ORDER BY m.createdAt DESC")
    List<MemberEntity> findRecentMembers(@Param("limit") int limit);
}
