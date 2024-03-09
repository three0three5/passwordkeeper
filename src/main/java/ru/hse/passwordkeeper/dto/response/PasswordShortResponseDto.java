package ru.hse.passwordkeeper.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class PasswordShortResponseDto {
    private UUID id;
    private String name;
    private String dir;
}
