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
import ru.hse.passwordkeeper.domain.repository.PasswordRepository;
import ru.hse.passwordkeeper.domain.repository.SharedPasswordsRepository;
import ru.hse.passwordkeeper.domain.repository.UserRepository;
import ru.hse.passwordkeeper.dto.request.LifeExpectancyRequestDto;
import ru.hse.passwordkeeper.dto.request.PasswordRequestDto;
import ru.hse.passwordkeeper.dto.request.SignInRequestDto;
import ru.hse.passwordkeeper.dto.request.SignUpRequestDto;
import ru.hse.passwordkeeper.dto.response.JwtAuthenticationResponseDto;
import ru.hse.passwordkeeper.dto.response.PasswordFullResponseDto;
import ru.hse.passwordkeeper.dto.response.PasswordShortResponseDto;
import ru.hse.passwordkeeper.dto.response.ShareTokenResponseDto;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ShareControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PasswordRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SharedPasswordsRepository sharedPasswordsRepository;

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
        sharedPasswordsRepository.deleteAll();
        repository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void givenValidPasswordRecord_whenShare_thenGetToken() {
        PasswordRequestDto dto = new PasswordRequestDto()
                .setName(RandomStringUtils.random(10, true, true))
                .setPassword(RandomStringUtils.random(10, true, true))
                .setUrl(RandomStringUtils.random(10, true, true))
                .setLogin(RandomStringUtils.random(10, true, true));

        HttpEntity<?> requestEntity = new HttpEntity<>(dto, headers);

        ResponseEntity<PasswordShortResponseDto> response =
                restTemplate.exchange("/passwords/", HttpMethod.POST, requestEntity, PasswordShortResponseDto.class);

        assertNotNull(response.getBody());
        UUID recordId = response.getBody().getId();

        requestEntity = new HttpEntity<>(new LifeExpectancyRequestDto(), headers);
        ResponseEntity<ShareTokenResponseDto> sharedResponse =
                restTemplate.exchange("/passwords/{id}/share",
                        HttpMethod.POST,
                        requestEntity,
                        ShareTokenResponseDto.class,
                        recordId);

        assertNotNull(sharedResponse.getBody());
        assertEquals(sharedResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(sharedResponse.getBody().getToken());
    }

    @Test
    public void givenTwoUsers_whenOneSharesToOther_thenRecordCopied() {
        String recordName = RandomStringUtils.random(10, true, true);
        String recordPassword = RandomStringUtils.random(10, true, true);
        String recordUrl = RandomStringUtils.random(10, true, true);
        String recordLogin = RandomStringUtils.random(10, true, true);
        PasswordRequestDto dto = new PasswordRequestDto()
                .setName(recordName)
                .setPassword(recordPassword)
                .setUrl(recordUrl)
                .setLogin(recordLogin);

        HttpEntity<?> requestEntity = new HttpEntity<>(dto, headers);

        ResponseEntity<PasswordShortResponseDto> response =
                restTemplate.exchange("/passwords/", HttpMethod.POST, requestEntity, PasswordShortResponseDto.class);

        assertNotNull(response.getBody());
        UUID recordId = response.getBody().getId();

        requestEntity = new HttpEntity<>(new LifeExpectancyRequestDto(), headers);
        ResponseEntity<ShareTokenResponseDto> sharedResponse =
                restTemplate.exchange("/passwords/{id}/share",
                        HttpMethod.POST,
                        requestEntity,
                        ShareTokenResponseDto.class,
                        recordId);

        assertNotNull(sharedResponse.getBody());
        assertEquals(sharedResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(sharedResponse.getBody().getToken());

        String otherUserName = RandomStringUtils.random(10, true, true);
        String otherPassword = RandomStringUtils.random(10, true, true);
        String otherUserJwt = obtainJwtToken(otherUserName, otherPassword);

        HttpHeaders otherHeaders = new HttpHeaders();
        otherHeaders.set("Authorization", "Bearer " + otherUserJwt);
        requestEntity = new HttpEntity<>(otherHeaders);

        ResponseEntity<PasswordFullResponseDto> copiedRecordResponse =
                restTemplate.exchange("/passwords/get_shared/{token}",
                        HttpMethod.GET,
                        requestEntity,
                        PasswordFullResponseDto.class,
                        sharedResponse.getBody().getToken());

        assertNotNull(copiedRecordResponse.getBody());
        assertEquals(copiedRecordResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(copiedRecordResponse.getBody().getName(), recordName);
        assertEquals(copiedRecordResponse.getBody().getPassword(), recordPassword);
        assertEquals(copiedRecordResponse.getBody().getUrl(), recordUrl);
        assertEquals(copiedRecordResponse.getBody().getLogin(), recordLogin);
    }

    @Test
    public void givenSharedPassword_whenUsedBySameUser_thenNotFound() {
        String recordName = RandomStringUtils.random(10, true, true);
        String recordPassword = RandomStringUtils.random(10, true, true);
        String recordUrl = RandomStringUtils.random(10, true, true);
        String recordLogin = RandomStringUtils.random(10, true, true);
        PasswordRequestDto dto = new PasswordRequestDto()
                .setName(recordName)
                .setPassword(recordPassword)
                .setUrl(recordUrl)
                .setLogin(recordLogin);

        HttpEntity<?> requestEntity = new HttpEntity<>(dto, headers);

        ResponseEntity<PasswordShortResponseDto> response =
                restTemplate.exchange("/passwords/", HttpMethod.POST, requestEntity, PasswordShortResponseDto.class);

        assertNotNull(response.getBody());
        UUID recordId = response.getBody().getId();

        requestEntity = new HttpEntity<>(new LifeExpectancyRequestDto(), headers);
        ResponseEntity<ShareTokenResponseDto> sharedResponse =
                restTemplate.exchange("/passwords/{id}/share",
                        HttpMethod.POST,
                        requestEntity,
                        ShareTokenResponseDto.class,
                        recordId);

        assertNotNull(sharedResponse.getBody());
        assertEquals(sharedResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(sharedResponse.getBody().getToken());

        ResponseEntity<PasswordFullResponseDto> copiedRecordResponse =
                restTemplate.exchange("/passwords/get_shared/{token}",
                        HttpMethod.GET,
                        requestEntity,
                        PasswordFullResponseDto.class,
                        sharedResponse.getBody().getToken());
        assertEquals(copiedRecordResponse.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void givenSharedPassword_whenUsed_thenNotFound() {
        String recordName = RandomStringUtils.random(10, true, true);
        String recordPassword = RandomStringUtils.random(10, true, true);
        String recordUrl = RandomStringUtils.random(10, true, true);
        String recordLogin = RandomStringUtils.random(10, true, true);
        PasswordRequestDto dto = new PasswordRequestDto()
                .setName(recordName)
                .setPassword(recordPassword)
                .setUrl(recordUrl)
                .setLogin(recordLogin);

        HttpEntity<?> requestEntity = new HttpEntity<>(dto, headers);

        ResponseEntity<PasswordShortResponseDto> response =
                restTemplate.exchange("/passwords/", HttpMethod.POST, requestEntity, PasswordShortResponseDto.class);

        assertNotNull(response.getBody());
        UUID recordId = response.getBody().getId();

        requestEntity = new HttpEntity<>(new LifeExpectancyRequestDto(), headers);
        ResponseEntity<ShareTokenResponseDto> sharedResponse =
                restTemplate.exchange("/passwords/{id}/share",
                        HttpMethod.POST,
                        requestEntity,
                        ShareTokenResponseDto.class,
                        recordId);

        assertNotNull(sharedResponse.getBody());
        assertEquals(sharedResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(sharedResponse.getBody().getToken());

        String otherUserName = RandomStringUtils.random(10, true, true);
        String otherPassword = RandomStringUtils.random(10, true, true);
        String otherUserJwt = obtainJwtToken(otherUserName, otherPassword);

        HttpHeaders otherHeaders = new HttpHeaders();
        otherHeaders.set("Authorization", "Bearer " + otherUserJwt);
        requestEntity = new HttpEntity<>(otherHeaders);

        ResponseEntity<PasswordFullResponseDto> copiedRecordResponse =
                restTemplate.exchange("/passwords/get_shared/{token}",
                        HttpMethod.GET,
                        requestEntity,
                        PasswordFullResponseDto.class,
                        sharedResponse.getBody().getToken());

        assertNotNull(copiedRecordResponse.getBody());
        assertEquals(copiedRecordResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(copiedRecordResponse.getBody().getName(), recordName);
        assertEquals(copiedRecordResponse.getBody().getPassword(), recordPassword);
        assertEquals(copiedRecordResponse.getBody().getUrl(), recordUrl);
        assertEquals(copiedRecordResponse.getBody().getLogin(), recordLogin);

        otherUserName = RandomStringUtils.random(10, true, true);
        otherPassword = RandomStringUtils.random(10, true, true);
        otherUserJwt = obtainJwtToken(otherUserName, otherPassword);

        otherHeaders = new HttpHeaders();
        otherHeaders.set("Authorization", "Bearer " + otherUserJwt);
        requestEntity = new HttpEntity<>(otherHeaders);

        copiedRecordResponse =
                restTemplate.exchange("/passwords/get_shared/{token}",
                        HttpMethod.GET,
                        requestEntity,
                        PasswordFullResponseDto.class,
                        sharedResponse.getBody().getToken());

        assertEquals(copiedRecordResponse.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void givenTwoUsers_whenOneGetsAllPasswords_thenOthersPasswordsAreNotAccessed() {
        String recordName = RandomStringUtils.random(10, true, true);
        String recordPassword = RandomStringUtils.random(10, true, true);
        String recordUrl = RandomStringUtils.random(10, true, true);
        String recordLogin = RandomStringUtils.random(10, true, true);
        PasswordRequestDto dto = new PasswordRequestDto()
                .setName(recordName)
                .setPassword(recordPassword)
                .setUrl(recordUrl)
                .setLogin(recordLogin);

        HttpEntity<?> requestEntity = new HttpEntity<>(dto, headers);

        ResponseEntity<PasswordShortResponseDto> response =
                restTemplate.exchange("/passwords/", HttpMethod.POST, requestEntity, PasswordShortResponseDto.class);

        assertNotNull(response.getBody());
        UUID recordId = response.getBody().getId();

        requestEntity = new HttpEntity<>(new LifeExpectancyRequestDto(), headers);
        ResponseEntity<ShareTokenResponseDto> sharedResponse =
                restTemplate.exchange("/passwords/{id}/share",
                        HttpMethod.POST,
                        requestEntity,
                        ShareTokenResponseDto.class,
                        recordId);

        assertNotNull(sharedResponse.getBody());
        assertEquals(sharedResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(sharedResponse.getBody().getToken());

        String otherUserName = RandomStringUtils.random(10, true, true);
        String otherPassword = RandomStringUtils.random(10, true, true);
        String otherUserJwt = obtainJwtToken(otherUserName, otherPassword);

        HttpHeaders otherHeaders = new HttpHeaders();
        otherHeaders.set("Authorization", "Bearer " + otherUserJwt);
        requestEntity = new HttpEntity<>(otherHeaders);

        ResponseEntity<List<PasswordShortResponseDto>> passwordsResponse = restTemplate.exchange(
                "/passwords/",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(passwordsResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(passwordsResponse.getBody());
        assertEquals(passwordsResponse.getBody().size(), 0);
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
