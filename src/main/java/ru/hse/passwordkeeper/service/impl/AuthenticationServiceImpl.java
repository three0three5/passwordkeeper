package ru.hse.passwordkeeper.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.hse.passwordkeeper.domain.entity.Role;
import ru.hse.passwordkeeper.domain.entity.UserEntity;
import ru.hse.passwordkeeper.domain.repository.UserRepository;
import ru.hse.passwordkeeper.dto.request.SignInRequestDto;
import ru.hse.passwordkeeper.dto.request.SignUpRequestDto;
import ru.hse.passwordkeeper.dto.response.JwtAuthenticationResponseDto;
import ru.hse.passwordkeeper.service.AuthenticationService;
import ru.hse.passwordkeeper.service.JwtService;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;

    @Override
    public JwtAuthenticationResponseDto signUp(SignUpRequestDto request) {
        var userInRepository = userRepository.findByLogin(request.getLogin());
        if (userInRepository != null) {
            throw new IllegalArgumentException("Login is not unique");
        }
        var user = new UserEntity();
        user.setUserRole(Role.ROLE_USER);
        user.setLogin(request.getLogin());
        user.setHashedPassword(encoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new JwtAuthenticationResponseDto(token);
    }

    @Override
    public JwtAuthenticationResponseDto signIn(SignInRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Invalid login or password.");
        }

        var user = userRepository.findByLogin(request.getLogin());

        String token = jwtService.generateToken(user);
        return new JwtAuthenticationResponseDto(token);
    }
}
