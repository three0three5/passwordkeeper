package ru.hse.passwordkeeper.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.hse.passwordkeeper.domain.repository.SharedPasswordsRepository;
import ru.hse.passwordkeeper.service.TokenCleanerService;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenCleanerServiceImpl implements TokenCleanerService {
    private final SharedPasswordsRepository repository;

    @Value("${scheduler.batch_size}")
    private final int batchSize;

    @Scheduled(cron = "${scheduler.cron}")
    @Override
    public void cleanTokens() {
        int affected = repository.deleteInactiveTokens(batchSize);
        log.info("Deleted inactive tokens: " + affected);
    }
}
