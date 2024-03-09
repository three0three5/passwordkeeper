package ru.hse.passwordkeeper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.RandomStringUtils;
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
import ru.hse.passwordkeeper.domain.entity.PasswordRecord;
import ru.hse.passwordkeeper.domain.repository.PasswordRepository;
import ru.hse.passwordkeeper.domain.repository.UserRepository;
import ru.hse.passwordkeeper.dto.request.PasswordRequestDto;
import ru.hse.passwordkeeper.dto.request.SignInRequestDto;
import ru.hse.passwordkeeper.dto.request.SignUpRequestDto;
import ru.hse.passwordkeeper.dto.response.JwtAuthenticationResponseDto;
import ru.hse.passwordkeeper.dto.response.PasswordShortResponseDto;
import ru.hse.passwordkeeper.dto.response.PasswordFullResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PasswordControllerIntegrationTest {
	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private PasswordRepository repository;

	@Autowired
	private UserRepository userRepository;
	private final HttpHeaders headers = new HttpHeaders();

	private final String ownerLogin = RandomStringUtils.random(10, true, true);

	private final String ownerPassword = RandomStringUtils.random(10, true, true);

	@BeforeEach
	public void setUp() {
		repository.deleteAll();
		String token = obtainJwtToken(ownerLogin, ownerPassword);
		headers.set("Authorization", "Bearer " + token);
	}

	@Test
	public void whenCreateRecord_thenSavedAndStatus200() {
		PasswordRequestDto dto = new PasswordRequestDto()
				.setName("test")
				.setPassword("password")
				.setUrl("url")
				.setLogin("login");

		HttpEntity<PasswordRequestDto> requestEntity = new HttpEntity<>(dto, headers);

		ResponseEntity<PasswordShortResponseDto> response =
				restTemplate.exchange("/passwords/", HttpMethod.POST, requestEntity, PasswordShortResponseDto.class);

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertNotNull(response.getBody());
		assertNotNull(response.getBody().getName());
		assertEquals(response.getBody().getName(), dto.getName());
	}

	@Test
	public void whenCreateRecordWithNullableFields_thenSavedAndStatus200() {
		PasswordRequestDto dto = new PasswordRequestDto()
				.setName("other")
				.setPassword("PassWord");

		HttpEntity<PasswordRequestDto> requestEntity = new HttpEntity<>(dto, headers);

		ResponseEntity<PasswordShortResponseDto> response =
				restTemplate.exchange("/passwords/", HttpMethod.POST, requestEntity, PasswordShortResponseDto.class);

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertNotNull(response.getBody());
		assertNotNull(response.getBody().getName());
		assertEquals(response.getBody().getName(), dto.getName());
	}

	@Test
	public void whenCreateRecord_thenSavedInRepository() {
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

		Optional<PasswordRecord> record = repository.findById(response.getBody().getId());
		assertTrue(record.isPresent());

		PasswordRecord result = record.get();
		assertEquals(result.getId(), response.getBody().getId());
		assertEquals(result.getName(), dto.getName());
		assertNull(result.getUrl());
		assertNull(result.getLogin());
	}

	@Test
	public void whenRequestHasNullPassword_thenReturn400() {
		PasswordRequestDto dto = new PasswordRequestDto()
				.setName("qwerty");

		HttpEntity<PasswordRequestDto> requestEntity = new HttpEntity<>(dto, headers);

		ResponseEntity<PasswordShortResponseDto> response =
				restTemplate.exchange("/passwords/", HttpMethod.POST, requestEntity, PasswordShortResponseDto.class);

		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
	}

	@Test
	public void whenRequestHasNullName_thenReturn400() {
		PasswordRequestDto dto = new PasswordRequestDto()
				.setPassword("qwerty");

		HttpEntity<PasswordRequestDto> requestEntity = new HttpEntity<>(dto, headers);

		ResponseEntity<PasswordShortResponseDto> response =
				restTemplate.exchange("/passwords/", HttpMethod.POST, requestEntity, PasswordShortResponseDto.class);

		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
	}

	@Test
	public void whenFindByIdThatIsPresent_thenReturnRecord() {
		PasswordRecord record = new PasswordRecord();
		record.setName("someName");
		record.setPassword("some password");
		record.setOwner(userRepository.findByLogin(ownerLogin));

		PasswordRecord saved = repository.save(record);
		assertNotNull(saved);

		HttpEntity<String> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<PasswordFullResponseDto> response =
				restTemplate.exchange("/passwords/{id}",
						HttpMethod.GET,
						requestEntity,
						PasswordFullResponseDto.class,
						saved.getId());

		assertEquals(response.getStatusCode(), HttpStatus.OK);

		assertNotNull(response.getBody());
		assertEquals(response.getBody().getPassword(), record.getPassword());
		assertEquals(response.getBody().getName(), record.getName());
		assertEquals(response.getBody().getLogin(), record.getLogin());
		assertEquals(response.getBody().getUrl(), record.getUrl());
		assertEquals(response.getBody().getId(), record.getId());
	}

	@Test
	public void whenFindByIdThatIsNotPresent_thenReturn404() {
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<PasswordRecord> response =
				restTemplate.exchange("/passwords/{id}", HttpMethod.GET,
						requestEntity,
						PasswordRecord.class,
						UUID.randomUUID());

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	public void givenEmptyRepository_whenFindAll_thenReturnEmptyList() {
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<List<PasswordShortResponseDto>> response = restTemplate.exchange(
				"/passwords/",
				HttpMethod.GET,
				requestEntity,
				new ParameterizedTypeReference<>() {}
		);

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertNotNull(response.getBody());
		assertEquals(response.getBody().size(), 0);
	}


	@Test
	public void givenNotEmptyRepository_whenFindAll_thenReturnAll() {
		PasswordRecord first = new PasswordRecord();
		first.setName("first");
		first.setPassword("p");
		first.setOwner(userRepository.findByLogin(ownerLogin));
		PasswordRecord second = new PasswordRecord();
		second.setName("second");
		second.setPassword("a");
		second.setOwner(userRepository.findByLogin(ownerLogin));
		PasswordRecord third = new PasswordRecord();
		third.setName("third");
		third.setPassword("b");
		third.setOwner(userRepository.findByLogin(ownerLogin));
		repository.save(first);
		repository.save(second);
		repository.save(third);

		HttpEntity<PasswordRequestDto> requestEntity = new HttpEntity<>(headers);
		ResponseEntity<List<PasswordShortResponseDto>> response = restTemplate.exchange(
				"/passwords/",
				HttpMethod.GET,
				requestEntity,
				new ParameterizedTypeReference<>() {}
		);

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertNotNull(response.getBody());
		assertEquals(response.getBody().size(), 3);
		assertEquals(response.getBody().get(0).getName(), first.getName());
		assertEquals(response.getBody().get(1).getName(), second.getName());
		assertEquals(response.getBody().get(2).getName(), third.getName());
	}

	@Test
	public void givenNotEmptyRepository_whenFindFirstPage_thenReturnPage() {
		PasswordRecord first = new PasswordRecord();
		first.setName("first");
		first.setPassword("p");
		first.setOwner(userRepository.findByLogin(ownerLogin));
		PasswordRecord second = new PasswordRecord();
		second.setName("second");
		second.setPassword("a");
		second.setOwner(userRepository.findByLogin(ownerLogin));
		PasswordRecord third = new PasswordRecord();
		third.setName("third");
		third.setPassword("b");
		third.setOwner(userRepository.findByLogin(ownerLogin));
		repository.save(first);
		repository.save(second);
		repository.save(third);

		HttpEntity<String> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.exchange(
				"/passwords/paginated?page=0&size=2", HttpMethod.GET, requestEntity, String.class);

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertNotNull(response.getBody());

		String jsonResponse = response.getBody();
		JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
		jsonResponse = jsonObject.get("content").toString();
		Gson gson = new Gson();
		TypeToken<List<PasswordShortResponseDto>> token = new TypeToken<>() {};

		List<PasswordShortResponseDto> result = gson.fromJson(jsonResponse, token.getType());
		assertEquals(result.size(), 2);
		assertEquals(result.get(0).getName(), first.getName());
		assertEquals(result.get(1).getName(), second.getName());
	}


	@Test
	public void givenAnExistingRecord_whenSave_thenUpdate() {
		PasswordRecord first = new PasswordRecord();
		first.setName("NAME");
		first.setPassword("COCONUT");
		first.setOwner(userRepository.findByLogin(ownerLogin));
		PasswordRecord record = repository.save(first);
		assertNotNull(record);

		record.setName("Other name");

		PasswordRequestDto toUpdate = new PasswordRequestDto()
				.setName("real name")
				.setLogin("login");

		HttpEntity<PasswordRequestDto> requestEntity = new HttpEntity<>(toUpdate, headers);

		ResponseEntity<PasswordFullResponseDto> response =
				restTemplate.exchange("/passwords/{id}",
						HttpMethod.PUT, requestEntity, PasswordFullResponseDto.class, record.getId());

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertNotNull(response.getBody());
		assertEquals(response.getBody().getId(), record.getId());
		assertEquals(response.getBody().getPassword(), first.getPassword());
		assertEquals(response.getBody().getName(), toUpdate.getName());
		assertEquals(response.getBody().getLogin(), toUpdate.getLogin());
	}

	@Test
	public void givenNotExistingRecord_whenSave_thenReturn404() {
		PasswordRequestDto toUpdate = new PasswordRequestDto()
				.setName("real name")
				.setLogin("login");

		HttpEntity<PasswordRequestDto> requestEntity = new HttpEntity<>(toUpdate, headers);

		ResponseEntity<PasswordRecord> response =
				restTemplate.exchange("/passwords/{id}",
						HttpMethod.PUT, requestEntity, PasswordRecord.class, UUID.randomUUID());

		assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
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
