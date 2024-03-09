package ru.hse.passwordkeeper.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.passwordkeeper.dto.request.PasswordRequestDto;
import ru.hse.passwordkeeper.dto.response.PasswordFullResponseDto;
import ru.hse.passwordkeeper.dto.response.PasswordShortResponseDto;
import ru.hse.passwordkeeper.service.PasswordService;
import ru.hse.passwordkeeper.utils.SimpleExceptionMessagesCreator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/passwords")
public class PasswordController {
    private final PasswordService service;
    private final SimpleExceptionMessagesCreator exceptionMessagesCreator;

    @PostMapping("/")
    public ResponseEntity<PasswordShortResponseDto> createPasswordRecord(
            @Valid @RequestBody PasswordRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails.getUsername();
        PasswordShortResponseDto result = service.createPasswordRecord(requestDto, username);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/")
    public ResponseEntity<List<PasswordShortResponseDto>> getAllPasswordRecords(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String directoryId) {
        List<PasswordShortResponseDto> result = service.getAllRecords(userDetails.getUsername(), directoryId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PasswordFullResponseDto> getRecordById(@PathVariable UUID id,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        Optional<PasswordFullResponseDto> result = service.findById(id, userDetails.getUsername());
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<PasswordShortResponseDto>>
            getPaginatedPasswordRecords(Pageable pageable, @AuthenticationPrincipal UserDetails userDetails,
                                        @RequestParam(required = false) String directoryId) {
        Page<PasswordShortResponseDto> result =
                service.getPaginatedRecords(pageable, userDetails.getUsername(), directoryId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PasswordFullResponseDto> updateRecord(@PathVariable UUID id,
                                                                @RequestBody PasswordRequestDto requestDto,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        Optional<PasswordFullResponseDto> result = service.updateById(id, requestDto, userDetails.getUsername());
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<PasswordFullResponseDto> changeDirectory(@PathVariable UUID id,
                                                                   @RequestParam(required = false) String directoryId,
                                                                   @AuthenticationPrincipal UserDetails userDetails) {
        Optional<PasswordFullResponseDto> result = service.changeDirectoryById(id, directoryId, userDetails.getUsername());
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        return exceptionMessagesCreator.getExceptionMessages(ex);
    }
}
