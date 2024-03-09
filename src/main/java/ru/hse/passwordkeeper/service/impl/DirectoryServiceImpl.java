package ru.hse.passwordkeeper.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import ru.hse.passwordkeeper.domain.entity.DirectoryEntity;
import ru.hse.passwordkeeper.domain.entity.UserEntity;
import ru.hse.passwordkeeper.domain.repository.DirectoryRepository;
import ru.hse.passwordkeeper.domain.repository.UserRepository;
import ru.hse.passwordkeeper.dto.response.DirectoryCreateResponseDto;
import ru.hse.passwordkeeper.dto.response.DirectoryFullResponseDto;
import ru.hse.passwordkeeper.service.DirectoryService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {
    private final DirectoryRepository directoryRepository;
    private final UserRepository userRepository;

    @Override
    public DirectoryEntity createNewDirectory(String parentId, String name, String owner) {
        UserEntity ownerUser = userRepository.findByLogin(owner);
        DirectoryEntity parent = getDirectory(parentId, ownerUser);
        DirectoryEntity toSave = new DirectoryEntity();
        toSave.setName(name);
        toSave.setParent(parent);
        String randomId = RandomStringUtils.randomAlphabetic(7);
        toSave.setId(randomId);
        toSave.setOwner(ownerUser);
        return directoryRepository.save(toSave);
    }

    @Override
    public List<DirectoryCreateResponseDto> getSubdirs(String id, String owner) {
        UserEntity ownerUser = userRepository.findByLogin(owner);
        DirectoryEntity parent = getDirectory(id, ownerUser);
        return directoryRepository.findByParentAndOwner(parent, ownerUser)
                .stream()
                .map(entity -> new DirectoryCreateResponseDto()
                        .setId(entity.getId()))
                .toList();
    }

    @Override
    public DirectoryFullResponseDto moveDirectory(String id, String moveTo, String owner) {
        UserEntity ownerUser = userRepository.findByLogin(owner);
        DirectoryEntity dir = getDirectory(id, ownerUser);
        if (dir == null) throw new IllegalArgumentException();
        DirectoryEntity newParent = getDirectory(moveTo, ownerUser);
        dir.setParent(newParent);
        dir = directoryRepository.save(dir);
        return mapToFullDto(dir);
    }

    private static DirectoryFullResponseDto mapToFullDto(DirectoryEntity entity) {
        if (entity == null) return null;
        String directory = null;
        if (entity.getParent() != null) directory = entity.getParent().getId();
        return new DirectoryFullResponseDto()
                .setParent(directory)
                .setId(entity.getId());
    }

    private DirectoryEntity getDirectory(String id, UserEntity ownerUser) {
        if (id == null) return null;
        Optional<DirectoryEntity> directory = directoryRepository.findByIdAndOwner(id, ownerUser);
        return directory.orElseThrow(IllegalArgumentException::new);
    }
}
