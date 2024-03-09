package ru.hse.passwordkeeper.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.hse.passwordkeeper.domain.entity.UserEntity;
import ru.hse.passwordkeeper.domain.repository.UserRepository;
import ru.hse.passwordkeeper.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDetailsService userDetailsService() {
        return (login) -> {
            var user = findByLogin(login);
            if (user == null) throw new UsernameNotFoundException(login + " not found");
            return user;
        };
    }

    @Override
    public UserEntity findByLogin(String login) {
        return userRepository.findByLogin(login);
    }
}
