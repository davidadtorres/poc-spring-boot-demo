package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.example.demo.model.Tutorial;
import com.example.demo.repository.TutorialRepository;

@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.MethodName.class)
class DemoApplicationIntegrationTests {
	static final int NUM_TUTORIALS = 12;

	@Autowired
	TutorialRepository tutorialRepository;

	@LocalServerPort
	private int port;

	TestRestTemplate restTemplate = new TestRestTemplate();

	HttpHeaders headers = new HttpHeaders();

	@Test
	@Sql(scripts = { "file:src/test/java/com/example/demo/test-tutorial-data.sql" })
	public void test1_populate_database() {
		assertThat(tutorialRepository.count()).isEqualTo(NUM_TUTORIALS);
	}

	private String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}

	@Test
	public void test2_retrieve_all_tutorials() {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<List<Tutorial>> response = restTemplate.exchange(createURLWithPort("/api/tutorials"),
				HttpMethod.GET, entity, new ParameterizedTypeReference<List<Tutorial>>() {
				});

		List<Tutorial> tutorials = response.getBody();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
		assertThat(tutorials).hasSize(NUM_TUTORIALS);

		Tutorial tutorial = tutorials.get(0);
		assertThat(tutorial).hasFieldOrPropertyWithValue("id", 1L);
		assertThat(tutorial).hasFieldOrPropertyWithValue("title", "Spring Boot Tut#1");
		assertThat(tutorial).hasFieldOrPropertyWithValue("description", "Desc for Tut#1");
		assertThat(tutorial).hasFieldOrPropertyWithValue("published", false);
	}

	@Test
	public void test3_retrieve_one_tutorial() {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<Tutorial> response = restTemplate.exchange(createURLWithPort("/api/tutorials/1"), HttpMethod.GET,
				entity, Tutorial.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

		Tutorial tutorial = response.getBody();
		assertThat(tutorial).hasFieldOrPropertyWithValue("id", 1L);
		assertThat(tutorial).hasFieldOrPropertyWithValue("title", "Spring Boot Tut#1");
		assertThat(tutorial).hasFieldOrPropertyWithValue("description", "Desc for Tut#1");
		assertThat(tutorial).hasFieldOrPropertyWithValue("published", false);
	}

	@Test
	public void test4_add_new_tutorial() throws URISyntaxException {
		Tutorial body = new Tutorial("Spring Boot Tut#13", "Desc for Tut#13", false);
		RequestEntity<Tutorial> request = RequestEntity.post(new URI(createURLWithPort("/api/tutorials")))
				.accept(MediaType.APPLICATION_JSON).body(body);

		ResponseEntity<Tutorial> response = restTemplate.exchange(request, Tutorial.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

		Tutorial tutorial = response.getBody();
		assertThat(tutorial).hasFieldOrPropertyWithValue("id", 13L);
		assertThat(tutorial).hasFieldOrPropertyWithValue("title", "Spring Boot Tut#13");
		assertThat(tutorial).hasFieldOrPropertyWithValue("description", "Desc for Tut#13");
		assertThat(tutorial).hasFieldOrPropertyWithValue("published", false);
	}

	@Test
	public void test5_update_tutorial() throws URISyntaxException {
		Tutorial body = new Tutorial("Spring Boot Tut#13 new", "Desc for Tut#13 new", false);
		RequestEntity<Tutorial> request = RequestEntity.put(new URI(createURLWithPort("/api/tutorials/13")))
				.accept(MediaType.APPLICATION_JSON).body(body);

		ResponseEntity<Tutorial> response = restTemplate.exchange(request, Tutorial.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

		Tutorial tutorial = response.getBody();
		assertThat(tutorial).hasFieldOrPropertyWithValue("id", 13L);
		assertThat(tutorial).hasFieldOrPropertyWithValue("title", "Spring Boot Tut#13 new");
		assertThat(tutorial).hasFieldOrPropertyWithValue("description", "Desc for Tut#13 new");
		assertThat(tutorial).hasFieldOrPropertyWithValue("published", false);
	}

	@Test
	public void test6_delete_tutorial() throws URISyntaxException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/api/tutorials/13"),
				HttpMethod.DELETE, entity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(tutorialRepository.count()).isEqualTo(NUM_TUTORIALS);
	}
}
