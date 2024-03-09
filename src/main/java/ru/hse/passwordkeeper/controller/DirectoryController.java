package ru.hse.passwordkeeper.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.passwordkeeper.dto.request.DirectoryCreateRequestDto;
import ru.hse.passwordkeeper.dto.response.DirectoryCreateResponseDto;
import ru.hse.passwordkeeper.dto.response.DirectoryFullResponseDto;
import ru.hse.passwordkeeper.service.DirectoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DirectoryController {
    private final DirectoryService directoryService;

    @PostMapping("/dirs")
    public ResponseEntity<DirectoryCreateResponseDto> createDirectory(
            @RequestBody DirectoryCreateRequestDto createRequestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(new DirectoryCreateResponseDto()
                .setId(directoryService.createNewDirectory(
                        createRequestDto.getId(),
                        createRequestDto.getName(),
                        userDetails.getUsername()
                ).getId())
        );
    }

    @GetMapping("/dirs")
    public ResponseEntity<List<DirectoryCreateResponseDto>> getAllSubdirectories(@RequestParam(required = false) String id,
                                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(directoryService.getSubdirs(id, userDetails.getUsername()));
    }

    @PatchMapping("/dirs/{id}")
    public ResponseEntity<DirectoryFullResponseDto> moveDirectory(@PathVariable String id,
                                                                  @RequestParam(required = false) String moveTo,
                                                                  @AuthenticationPrincipal UserDetails userDetails) {
        DirectoryFullResponseDto patched = directoryService.moveDirectory(id, moveTo, userDetails.getUsername());
        return ResponseEntity.ok(patched);
    }
}
