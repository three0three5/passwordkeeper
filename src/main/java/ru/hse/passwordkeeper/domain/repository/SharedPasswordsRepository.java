package ru.hse.passwordkeeper.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.hse.passwordkeeper.domain.entity.SharedPasswordEntity;
import ru.hse.passwordkeeper.domain.entity.UserEntity;

import java.util.UUID;

@Repository
public interface SharedPasswordsRepository extends JpaRepository<SharedPasswordEntity, UUID> {

    @Modifying
    @Transactional
    @Query("update SharedPasswordEntity set sharedWith=:toShareWith where " +
            " sharedWith is null and" +
            " (expiredAt is null or expiredAt < current_timestamp) and" +
            " owner != :toShareWith and" +
            " id = :token")
    int useToken(UUID token, UserEntity toShareWith);

    @Modifying
    @Transactional
    @Query("delete from SharedPasswordEntity s " +
            "where s.id in (select s.id from SharedPasswordEntity s " +
            "where s.expiredAt <= current_timestamp or s.sharedWith is not null " +
            "order by s.createdAt limit :limitValue)")
    int deleteInactiveTokens(int limitValue);
}
