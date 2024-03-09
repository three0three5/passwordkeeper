package ru.hse.passwordkeeper.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hse.passwordkeeper.domain.entity.DirectoryEntity;
import ru.hse.passwordkeeper.domain.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface DirectoryRepository extends JpaRepository<DirectoryEntity, String> {
    List<DirectoryEntity> findByParentAndOwner(DirectoryEntity parent, UserEntity owner);

    Optional<DirectoryEntity> findByIdAndOwner(String parentId, UserEntity byLogin);
}
