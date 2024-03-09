package ru.hse.passwordkeeper.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.hse.passwordkeeper.dto.request.PasswordRequestDto;
import ru.hse.passwordkeeper.dto.response.PasswordFullResponseDto;
import ru.hse.passwordkeeper.dto.response.PasswordShortResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordService {
    PasswordShortResponseDto createPasswordRecord(PasswordRequestDto requestDto, String owner);

    List<PasswordShortResponseDto> getAllRecords(String owner, String directoryId);

    Optional<PasswordFullResponseDto> findById(UUID id, String owner);

    Page<PasswordShortResponseDto> getPaginatedRecords(Pageable pageable, String owner, String directoryId);

    Optional<PasswordFullResponseDto> updateById(UUID id, PasswordRequestDto requestDto, String owner);

    Optional<PasswordFullResponseDto> changeDirectoryById(UUID id, String parent, String username);
}
