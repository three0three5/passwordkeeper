package ru.hse.passwordkeeper.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class PasswordFullResponseDto {
    private UUID id;
    private String name;

    private String login;

    private String password;

    private String url;

    private String dir;
}
