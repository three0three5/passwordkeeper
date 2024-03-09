package ru.hse.passwordkeeper;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import ru.hse.passwordkeeper.domain.repository.DirectoryRepository;
import ru.hse.passwordkeeper.domain.repository.PasswordRepository;
import ru.hse.passwordkeeper.domain.repository.UserRepository;
import ru.hse.passwordkeeper.dto.request.DirectoryCreateRequestDto;
import ru.hse.passwordkeeper.dto.request.PasswordRequestDto;
import ru.hse.passwordkeeper.dto.request.SignInRequestDto;
import ru.hse.passwordkeeper.dto.request.SignUpRequestDto;
import ru.hse.passwordkeeper.dto.response.DirectoryCreateResponseDto;
import ru.hse.passwordkeeper.dto.response.JwtAuthenticationResponseDto;
import ru.hse.passwordkeeper.dto.response.PasswordFullResponseDto;
import ru.hse.passwordkeeper.dto.response.PasswordShortResponseDto;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DirectoryControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private UserRepository userRepository;

    private final String ownerLogin = RandomStringUtils.random(10, true, true);
    private final String ownerPassword = RandomStringUtils.random(10, true, true);
    private final HttpHeaders headers = new HttpHeaders();

    @BeforeEach
    public void setUp() {
        String token = obtainJwtToken(ownerLogin, ownerPassword);
        headers.set("Authorization", "Bearer " + token);
    }

    @AfterEach
    public void cleanUp() {
        passwordRepository.deleteAll();
        directoryRepository.deleteAll();
    }

    @Test
    public void givenRootDir_whenCreateOtherDit_thenOkAndNonEmpty() {
        DirectoryCreateRequestDto createRequestDto = new DirectoryCreateRequestDto();
        HttpEntity<DirectoryCreateRequestDto> requestEntity = new HttpEntity<>(createRequestDto, headers);

        ResponseEntity<DirectoryCreateResponseDto> response = restTemplate.exchange(
                "/dirs", HttpMethod.POST, requestEntity, DirectoryCreateResponseDto.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
    }

    @Test
    public void givenRootDir_whenCreateOtherDit_thenFoundInRootDir() {
        DirectoryCreateRequestDto createRequestDto = new DirectoryCreateRequestDto();
        HttpEntity<DirectoryCreateRequestDto> requestEntity = new HttpEntity<>(createRequestDto, headers);

        ResponseEntity<DirectoryCreateResponseDto> response = restTemplate.exchange(
                "/dirs", HttpMethod.POST, requestEntity, DirectoryCreateResponseDto.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());

        String id = response.getBody().getId();

        ResponseEntity<List<DirectoryCreateResponseDto>> subdirs = restTemplate.exchange(
                "/dirs", HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {}
        );

        assertEquals(subdirs.getStatusCode(), HttpStatus.OK);
        assertNotNull(subdirs.getBody());
        assertEquals(subdirs.getBody().get(0).getId(), id);
    }

    @Test
    public void givenRecordWithNoDir_whenCreate_thenFoundInRoot() {
        PasswordRequestDto dto = new PasswordRequestDto()
                .setName("qwerty")
                .setPassword("asdfgh");

        HttpEntity<PasswordRequestDto> requestEntity = new HttpEntity<>(dto, headers);

        ResponseEntity<PasswordShortResponseDto> response =
                restTemplate.exchange("/passwords/", HttpMethod.POST, requestEntity, PasswordShortResponseDto.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getName());
        assertEquals(response.getBody().getName(), dto.getName());

        requestEntity = new HttpEntity<>(headers);

        ResponseEntity<List<PasswordShortResponseDto>> otherResponse = restTemplate.exchange(
                "/passwords/",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(otherResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(otherResponse.getBody());
        assertEquals(otherResponse.getBody().size(), 1);
        assertEquals(otherResponse.getBody().get(0).getName(), dto.getName());
        assertNull(otherResponse.getBody().get(0).getDir());
    }

    @Test
    public void givenRecordWithNoDir_whenCreate_thenNotFoundInOtherDirectory() {
        DirectoryCreateRequestDto createRequestDto = new DirectoryCreateRequestDto();
        HttpEntity<DirectoryCreateRequestDto> requestEntity = new HttpEntity<>(createRequestDto, headers);

        ResponseEntity<DirectoryCreateResponseDto> dirResponse = restTemplate.exchange(
                "/dirs", HttpMethod.POST, requestEntity, DirectoryCreateResponseDto.class);

        assertEquals(dirResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(dirResponse.getBody());

        String id = dirResponse.getBody().getId();

        PasswordRequestDto dto = new PasswordRequestDto()
                .setName("test")
                .setPassword("password")
                .setUrl("url")
                .setLogin("login");

        HttpEntity<PasswordRequestDto> pwrdRequest = new HttpEntity<>(dto, headers);

        ResponseEntity<PasswordShortResponseDto> response =
                restTemplate.exchange("/passwords/", HttpMethod.POST, pwrdRequest, PasswordShortResponseDto.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getName());
        assertEquals(response.getBody().getName(), dto.getName());

        requestEntity = new HttpEntity<>(headers);

        ResponseEntity<List<PasswordShortResponseDto>> otherResponse = restTemplate.exchange(
                "/passwords/?directoryId=" + id,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(otherResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(otherResponse.getBody());
        assertEquals(otherResponse.getBody().size(), 0);
    }

    @Test
    public void givenTwoDirectories_whenMovedRecord_thenSeenInOtherDirectory() {
        DirectoryCreateRequestDto createRequestDto = new DirectoryCreateRequestDto();
        HttpEntity<DirectoryCreateRequestDto> requestEntity = new HttpEntity<>(createRequestDto, headers);

        ResponseEntity<DirectoryCreateResponseDto> dirResponse = restTemplate.exchange(
                "/dirs", HttpMethod.POST, requestEntity, DirectoryCreateResponseDto.class);

        assertEquals(dirResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(dirResponse.getBody());

        String dir1 = dirResponse.getBody().getId();

        requestEntity = new HttpEntity<>(createRequestDto, headers);

        dirResponse = restTemplate.exchange(
                "/dirs", HttpMethod.POST, requestEntity, DirectoryCreateResponseDto.class);

        assertEquals(dirResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(dirResponse.getBody());

        String dir2 = dirResponse.getBody().getId();

        PasswordRequestDto dto = new PasswordRequestDto()
                .setName("test")
                .setPassword("password")
                .setUrl("url")
                .setLogin("login");

        HttpEntity<PasswordRequestDto> pwrdRequest = new HttpEntity<>(dto, headers);

        ResponseEntity<PasswordShortResponseDto> response =
                restTemplate.exchange("/passwords/?directoryId=" + dir1, HttpMethod.POST,
                        pwrdRequest, PasswordShortResponseDto.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getName());
        assertEquals(response.getBody().getName(), dto.getName());

        UUID recordId = response.getBody().getId();

        requestEntity = new HttpEntity<>(headers);

        ResponseEntity<PasswordFullResponseDto> fullResponse =
                restTemplate.exchange("/passwords/{id}/move?directoryId=" + dir2, HttpMethod.PUT,
                        requestEntity, PasswordFullResponseDto.class, recordId);

        assertEquals(fullResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(fullResponse.getBody());
        assertNotNull(fullResponse.getBody().getName());
        assertEquals(fullResponse.getBody().getName(), dto.getName());

        ResponseEntity<List<PasswordShortResponseDto>> otherResponse = restTemplate.exchange(
                "/passwords/?directoryId=" + dir2,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(otherResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(otherResponse.getBody());
        assertEquals(otherResponse.getBody().size(), 1);
    }

    private String obtainJwtToken(String username, String password) {
        if (userRepository.findByLogin(username) != null) {
            ResponseEntity<JwtAuthenticationResponseDto> response = restTemplate.postForEntity(
                    "/auth/signin",
                    new SignInRequestDto(username, password),
                    JwtAuthenticationResponseDto.class
            );
            return response.getBody().getToken();
        }
        ResponseEntity<JwtAuthenticationResponseDto> response = restTemplate.postForEntity(
                "/auth/signup",
                new SignUpRequestDto(username, password),
                JwtAuthenticationResponseDto.class
        );
        return response.getBody().getToken();
    }
}
