package ru.hse.passwordkeeper.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PasswordRequestDto {
    @NotNull(message = "Name value should be provided")
    private String name;

    private String login;

    @NotNull(message = "Password value should be provided")
    private String password;

    private String url;

    private String dir;
}
