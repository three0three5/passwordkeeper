package ru.hse.passwordkeeper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import ru.hse.passwordkeeper.domain.repository.PasswordRepository;
import ru.hse.passwordkeeper.domain.repository.UserRepository;
import ru.hse.passwordkeeper.dto.ErrorResponseDto;
import ru.hse.passwordkeeper.dto.request.PasswordRequestDto;
import ru.hse.passwordkeeper.dto.request.SignInRequestDto;
import ru.hse.passwordkeeper.dto.request.SignUpRequestDto;
import ru.hse.passwordkeeper.dto.response.JwtAuthenticationResponseDto;
import ru.hse.passwordkeeper.dto.response.PasswordShortResponseDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthenticationControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @BeforeEach
    public void setUp() {
        passwordRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void givenNotAuthenticatedUser_whenCreateVector_thenForbidden() {
        PasswordRequestDto dto = new PasswordRequestDto()
                .setName("volcano")
                .setPassword("jazz");
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<PasswordRequestDto> requestEntity = new HttpEntity<>(dto, headers);

        ResponseEntity<PasswordShortResponseDto> response =
                restTemplate.exchange("/passwords/create", HttpMethod.POST, requestEntity, PasswordShortResponseDto.class);

        assertEquals(response.getStatusCode(), HttpStatus.FORBIDDEN);
        assertNull(response.getBody());
    }

    @Test
    public void givenShortPassword_whenSignUp_thenIllegalPasswordMessageReceived() {
        String username = "usual login";
        String password = "short";

        ResponseEntity<ErrorResponseDto> response = restTemplate.postForEntity(
                "/auth/signup",
                new SignUpRequestDto(username, password),
                ErrorResponseDto.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.FORBIDDEN);
        assertNotNull(response.getBody());
        assertEquals(response.getBody().getPassword(), "Password must be at least 6 characters long");
    }

    @Test
    public void givenAcceptableLoginAndPassword_whenSignUp_thenTokenReceived () {
        String username = "usual login";
        String password = "normal password";

        ResponseEntity<JwtAuthenticationResponseDto> response = restTemplate.postForEntity(
                "/auth/signup",
                new SignUpRequestDto(username, password),
                JwtAuthenticationResponseDto.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
    }

    @Test
    public void givenCorrectLoginAndPassword_whenSignIn_thenTokenReceived () {
        String username = "usual login";
        String password = "normal password";

        restTemplate.postForEntity(
                "//auth/signup",
                new SignUpRequestDto(username, password),
                JwtAuthenticationResponseDto.class
        );

        ResponseEntity<JwtAuthenticationResponseDto> response = restTemplate.postForEntity(
                "/auth/signin",
                new SignInRequestDto(username, password),
                JwtAuthenticationResponseDto.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
    }

    @Test
    public void givenWrongLogin_whenSignIn_thenReceivedErrorMessage () {
        String username = "usual login", other = "wrong login";
        String password = "normal password";

        restTemplate.postForEntity(
                "/auth/signup",
                new SignUpRequestDto(username, password),
                JwtAuthenticationResponseDto.class
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/signin",
                new SignInRequestDto(other, password),
                String.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.FORBIDDEN);
        assertNotNull(response.getBody());
        assertEquals(response.getBody(), "Invalid login or password.");
    }

    @Test
    public void givenWrongPassword_whenSignIn_thenReceivedErrorMessage () {
        String username = "SuPeRnIcKnAmE";
        String password = "cool password", other = "cool  password";

        restTemplate.postForEntity(
                "/auth/signup",
                new SignUpRequestDto(username, password),
                JwtAuthenticationResponseDto.class
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/signin",
                new SignInRequestDto(username, other),
                String.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.FORBIDDEN);
        assertNotNull(response.getBody());
        assertEquals(response.getBody(), "Invalid login or password.");
    }

    @Test
    public void givenNotUniqueLogin_whenSignUp_thenReceivedErrorMessage () {
        String username = "omg nickname";
        String password = "cool password";

        restTemplate.postForEntity(
                "/auth/signup",
                new SignUpRequestDto(username, password),
                JwtAuthenticationResponseDto.class
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/signup",
                new SignInRequestDto(username, password),
                String.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.FORBIDDEN);
        assertNotNull(response.getBody());
        assertEquals(response.getBody(), "Login is not unique");
    }

    @Test
    public void givenCorrectCredentials_whenPostResource_thenHttpStatusOk () {
        String username = "qwerrtty";
        String password = "qwerty12345";

        restTemplate.postForEntity(
                "/auth/signup",
                new SignUpRequestDto(username, password),
                JwtAuthenticationResponseDto.class
        );

        ResponseEntity<JwtAuthenticationResponseDto> response = restTemplate.postForEntity(
                "/auth/signin",
                new SignUpRequestDto(username, password),
                JwtAuthenticationResponseDto.class
        );
        assertNotNull(response.getBody());
        assertEquals(response.getStatusCode(), HttpStatus.OK);

        String token = response.getBody().getToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        PasswordRequestDto dto = new PasswordRequestDto()
                .setName("example")
                .setPassword("of record");

        HttpEntity<PasswordRequestDto> requestEntity = new HttpEntity<>(dto, headers);

        ResponseEntity<PasswordShortResponseDto> otherResponse =
                restTemplate.exchange("/passwords/", HttpMethod.POST, requestEntity, PasswordShortResponseDto.class);

        assertEquals(otherResponse.getStatusCode(), HttpStatus.OK);
        assertNotNull(otherResponse.getBody());
        assertNotNull(otherResponse.getBody().getName());
        assertEquals(otherResponse.getBody().getName(), dto.getName());
    }
}
