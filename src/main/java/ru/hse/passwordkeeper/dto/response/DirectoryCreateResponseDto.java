package ru.hse.passwordkeeper.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DirectoryCreateResponseDto {
    private String id;
}
