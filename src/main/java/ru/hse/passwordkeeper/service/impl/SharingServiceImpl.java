package ru.hse.passwordkeeper.service.impl;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hse.passwordkeeper.domain.entity.PasswordRecord;
import ru.hse.passwordkeeper.domain.entity.SharedPasswordEntity;
import ru.hse.passwordkeeper.domain.repository.PasswordRepository;
import ru.hse.passwordkeeper.domain.repository.SharedPasswordsRepository;
import ru.hse.passwordkeeper.domain.repository.UserRepository;
import ru.hse.passwordkeeper.service.PasswordSharingService;

import java.sql.Date;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SharingServiceImpl implements PasswordSharingService {
    private final SharedPasswordsRepository sharedPasswordsRepository;
    private final UserRepository userRepository;
    private final PasswordRepository passwordRepository;
    private final MeterRegistry meterRegistry;
    private DistributionSummary distributionSummary;

    @PostConstruct
    public void setUp() {
        distributionSummary = DistributionSummary
                .builder("shared_record_usage_duration")
                .publishPercentiles(0.5, 0.8, 0.95, 0.99)
                .baseUnit("milliseconds")
                .register(meterRegistry);
    }

    @Override
    public SharedPasswordEntity createRecordForSharing(UUID passwordRecordId, String username, Long lifeExpectancy) {
        Optional<PasswordRecord> record = passwordRepository.findById(passwordRecordId);
        if (record.isEmpty()) throw new IllegalArgumentException("No record found with this UUID");
        SharedPasswordEntity shared = new SharedPasswordEntity();
        shared.setOwner(userRepository.findByLogin(username));
        shared.setToShare(record.get());
        if (lifeExpectancy != null) {
            long millis = lifeExpectancy + System.currentTimeMillis();
            shared.setExpiredAt(Date.from(Instant.ofEpochMilli(millis)));
        }
        return sharedPasswordsRepository.save(shared);
    }

    @Override
    @Transactional
    public Optional<PasswordRecord> getSharedRecord(UUID token, String username) {
        int affectedRows = sharedPasswordsRepository.useToken(token, userRepository.findByLogin(username));
        log.info("Affected by useToken: " + affectedRows);
        if (affectedRows == 0) {
            meterRegistry.counter("shared_record_invalid_token_attempts").increment();
            return Optional.empty();
        }
        SharedPasswordEntity used = sharedPasswordsRepository.findById(token).get();
        PasswordRecord toCopy = used.getToShare();
        PasswordRecord copied = copyRecord(toCopy);
        copied.setOwner(userRepository.findByLogin(username));

        log.info(String.valueOf(System.currentTimeMillis() - used.getCreatedAt().getTime()));
        distributionSummary.record(System.currentTimeMillis() - used.getCreatedAt().getTime());
        return Optional.of(passwordRepository.save(copied));
    }

    private PasswordRecord copyRecord(PasswordRecord toCopy) {
        PasswordRecord copied = new PasswordRecord();
        copied.setName(toCopy.getName());
        copied.setPassword(toCopy.getPassword());
        copied.setLogin(toCopy.getLogin());
        copied.setUrl(toCopy.getUrl());
        return copied;
    }
}
