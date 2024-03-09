package ru.hse.passwordkeeper.service;

import ru.hse.passwordkeeper.dto.request.SignInRequestDto;
import ru.hse.passwordkeeper.dto.request.SignUpRequestDto;
import ru.hse.passwordkeeper.dto.response.JwtAuthenticationResponseDto;

public interface AuthenticationService {
    JwtAuthenticationResponseDto signUp(SignUpRequestDto request);

    JwtAuthenticationResponseDto signIn(SignInRequestDto request);
}
