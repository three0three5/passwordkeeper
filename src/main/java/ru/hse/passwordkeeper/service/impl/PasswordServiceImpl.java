package ru.hse.passwordkeeper.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.hse.passwordkeeper.domain.entity.DirectoryEntity;
import ru.hse.passwordkeeper.domain.entity.PasswordRecord;
import ru.hse.passwordkeeper.domain.entity.UserEntity;
import ru.hse.passwordkeeper.domain.repository.DirectoryRepository;
import ru.hse.passwordkeeper.domain.repository.PasswordRepository;
import ru.hse.passwordkeeper.domain.repository.UserRepository;
import ru.hse.passwordkeeper.dto.request.PasswordRequestDto;
import ru.hse.passwordkeeper.dto.response.PasswordFullResponseDto;
import ru.hse.passwordkeeper.dto.response.PasswordShortResponseDto;
import ru.hse.passwordkeeper.service.PasswordService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {
    private final PasswordRepository repository;
    private final UserRepository userRepository;
    private final DirectoryRepository directoryRepository;

    public PasswordShortResponseDto createPasswordRecord(PasswordRequestDto requestDto, String ownerLogin) {
        UserEntity owner = userRepository.findByLogin(ownerLogin);
        DirectoryEntity directory = getDirectory(requestDto.getDir(), owner);
        PasswordRecord toSave = new PasswordRecord();

        toSave.setName(requestDto.getName());
        toSave.setPassword(requestDto.getPassword());
        toSave.setUrl(requestDto.getUrl());
        toSave.setLogin(requestDto.getLogin());
        toSave.setOwner(owner);
        toSave.setDirectory(directory);

        return mapToShortDto(repository.save(toSave));
    }

    public List<PasswordShortResponseDto> getAllRecords(String ownerLogin, String directoryId) {
        UserEntity owner = userRepository.findByLogin(ownerLogin);
        DirectoryEntity directory = getDirectory(directoryId, owner);
        return repository.findByOwnerAndDirectory(
                        owner,
                        directory)
                .stream()
                .map(this::mapToShortDto).toList();
    }

    public Optional<PasswordFullResponseDto> findById(UUID id, String owner) {
        var result = repository.findByIdAndOwner(id, userRepository.findByLogin(owner));
        return result.map(PasswordServiceImpl::mapToFullDto);
    }

    public Page<PasswordShortResponseDto> getPaginatedRecords(Pageable pageable, String ownerLogin, String directoryId) {
        UserEntity owner = userRepository.findByLogin(ownerLogin);
        DirectoryEntity directory = getDirectory(directoryId, owner);
        return repository.findByOwnerAndDirectory(owner, directory, pageable)
                .map(this::mapToShortDto);
    }

    public Optional<PasswordFullResponseDto> updateById(UUID id, PasswordRequestDto requestDto, String ownerLogin) {
        UserEntity owner = userRepository.findByLogin(ownerLogin);
        Optional<PasswordRecord> record = repository.findByIdAndOwner(id, owner);
        if (record.isEmpty()) {
            return Optional.empty();
        }
        PasswordRecord passwordRecord = record.get();
        if (requestDto.getPassword() != null) passwordRecord.setPassword(requestDto.getPassword());
        passwordRecord.setLogin(requestDto.getLogin());
        passwordRecord.setUrl(requestDto.getUrl());
        if (requestDto.getName() != null) passwordRecord.setName(requestDto.getName());
        DirectoryEntity parent = getDirectory(requestDto.getDir(), owner);
        passwordRecord.setDirectory(parent);
        passwordRecord = repository.save(passwordRecord);
        return Optional.of(mapToFullDto(passwordRecord));
    }

    @Override
    public Optional<PasswordFullResponseDto> changeDirectoryById(UUID id, String parent, String username) {
        UserEntity owner = userRepository.findByLogin(username);
        Optional<PasswordRecord> record = repository.findByIdAndOwner(id, userRepository.findByLogin(username));
        DirectoryEntity dir = getDirectory(parent, owner);
        if (parent != null && dir == null || record.isEmpty()) {
            return Optional.empty();
        }
        PasswordRecord toSave = record.get();
        toSave.setDirectory(dir);
        PasswordRecord saved = repository.save(toSave);
        return Optional.of(mapToFullDto(saved));
    }

    private DirectoryEntity getDirectory(String id, UserEntity ownerUser) {
        if (id == null) return null;
        Optional<DirectoryEntity> directory = directoryRepository.findByIdAndOwner(id, ownerUser);
        return directory.orElseThrow(IllegalArgumentException::new);
    }

    private static PasswordFullResponseDto mapToFullDto(PasswordRecord passwordRecord) {
        if (passwordRecord == null) return null;
        String directory = null;
        if (passwordRecord.getDirectory() != null) directory = passwordRecord.getDirectory().getId();
        return new PasswordFullResponseDto()
                .setPassword(passwordRecord.getPassword())
                .setDir(directory)
                .setUrl(passwordRecord.getUrl())
                .setLogin(passwordRecord.getLogin())
                .setName(passwordRecord.getName())
                .setId(passwordRecord.getId());
    }

    private PasswordShortResponseDto mapToShortDto(PasswordRecord entity) {
        if (entity == null) return null;
        String directory = null;
        if (entity.getDirectory() != null) directory = entity.getDirectory().getId();
        return new PasswordShortResponseDto()
                .setDir(directory)
                .setName(entity.getName())
                .setId(entity.getId());
    }
}
