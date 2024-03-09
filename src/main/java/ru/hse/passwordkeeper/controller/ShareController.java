package ru.hse.passwordkeeper.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.passwordkeeper.domain.entity.PasswordRecord;
import ru.hse.passwordkeeper.domain.entity.SharedPasswordEntity;
import ru.hse.passwordkeeper.dto.request.LifeExpectancyRequestDto;
import ru.hse.passwordkeeper.dto.response.PasswordFullResponseDto;
import ru.hse.passwordkeeper.dto.response.ShareTokenResponseDto;
import ru.hse.passwordkeeper.service.PasswordSharingService;
import ru.hse.passwordkeeper.utils.SimpleExceptionMessagesCreator;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/passwords")
public class ShareController {
    private final PasswordSharingService sharingService;
    private final SimpleExceptionMessagesCreator exceptionMessagesCreator;


    @PostMapping("/{id}/share")
    public ResponseEntity<ShareTokenResponseDto> sharePassword(
            @PathVariable UUID id,
            @Valid @RequestBody LifeExpectancyRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SharedPasswordEntity result = sharingService.createRecordForSharing(
                id,
                userDetails.getUsername(),
                requestDto.getLifeExpectancy()
        );
        return ResponseEntity.ok(
                new ShareTokenResponseDto().setToken(result.getId())
        );
    }

    @GetMapping("/get_shared/{token}")
    public ResponseEntity<PasswordFullResponseDto> getSharedPassword(
            @PathVariable UUID token,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Optional<PasswordRecord> result = sharingService.getSharedRecord(token, userDetails.getUsername());
        return result.map((r) -> ResponseEntity.ok(
                    new PasswordFullResponseDto()
                            .setPassword(result.get().getPassword())
                            .setUrl(result.get().getUrl())
                            .setName(result.get().getName())
                            .setId(result.get().getId())
                            .setLogin(result.get().getLogin()) // TODO маппер
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        return exceptionMessagesCreator.getExceptionMessages(ex);
    }
}
