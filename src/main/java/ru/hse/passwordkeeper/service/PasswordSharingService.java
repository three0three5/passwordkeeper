package ru.hse.passwordkeeper.service;

import ru.hse.passwordkeeper.domain.entity.PasswordRecord;
import ru.hse.passwordkeeper.domain.entity.SharedPasswordEntity;

import java.util.Optional;
import java.util.UUID;

public interface PasswordSharingService {

    SharedPasswordEntity createRecordForSharing(UUID passwordRecordId, String username, Long lifeExpectancy);

    Optional<PasswordRecord> getSharedRecord(UUID token, String username);
}
