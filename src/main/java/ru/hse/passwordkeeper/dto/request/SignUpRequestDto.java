package ru.hse.passwordkeeper.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {
    @NotNull(message = "Login should be provided")
    private String login;

    @NotNull(message = "Password should be provided")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
}
