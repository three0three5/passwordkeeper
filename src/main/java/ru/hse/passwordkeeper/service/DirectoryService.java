package ru.hse.passwordkeeper.service;

import ru.hse.passwordkeeper.domain.entity.DirectoryEntity;
import ru.hse.passwordkeeper.dto.response.DirectoryCreateResponseDto;
import ru.hse.passwordkeeper.dto.response.DirectoryFullResponseDto;

import java.util.List;

public interface DirectoryService {

    DirectoryEntity createNewDirectory(String id, String name, String owner);

    List<DirectoryCreateResponseDto> getSubdirs(String id, String owner);

    DirectoryFullResponseDto moveDirectory(String id, String moveTo, String owner);
}
