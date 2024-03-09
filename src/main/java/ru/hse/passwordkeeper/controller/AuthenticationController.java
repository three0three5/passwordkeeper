package ru.hse.passwordkeeper.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.passwordkeeper.dto.request.SignInRequestDto;
import ru.hse.passwordkeeper.dto.request.SignUpRequestDto;
import ru.hse.passwordkeeper.service.AuthenticationService;
import ru.hse.passwordkeeper.utils.SimpleExceptionMessagesCreator;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final SimpleExceptionMessagesCreator exceptionMessagesCreator;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequestDto request) {
        try {
            return ResponseEntity.ok(authenticationService.signUp(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequestDto request) {
        try {
            return ResponseEntity.ok(authenticationService.signIn(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        return exceptionMessagesCreator.getExceptionMessages(ex);
    }
}
