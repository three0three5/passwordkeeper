package ru.hse.passwordkeeper.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hse.passwordkeeper.domain.entity.DirectoryEntity;
import ru.hse.passwordkeeper.domain.entity.PasswordRecord;
import ru.hse.passwordkeeper.domain.entity.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordRepository extends JpaRepository<PasswordRecord, UUID> {

    List<PasswordRecord> findByOwnerAndDirectory(UserEntity owner, DirectoryEntity directory);

    Optional<PasswordRecord> findByIdAndOwner(UUID id, UserEntity owner);

    Page<PasswordRecord> findByOwnerAndDirectory(UserEntity owner, DirectoryEntity directory, Pageable pageable);
}
