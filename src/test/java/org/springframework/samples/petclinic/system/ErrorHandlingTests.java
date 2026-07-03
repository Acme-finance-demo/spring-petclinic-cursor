/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

/**
 * Integration Test for error handling and information disclosure prevention.
 *
 * @author Security Team
 */
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = { "management.endpoints.enabled-by-default=false" })
class ErrorHandlingTests {

	@Value(value = "${local.server.port}")
	private int port;

	@Autowired
	private TestRestTemplate rest;

	@Test
	void testErrorPageDoesNotExposeExceptionMessage() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.TEXT_HTML));
		ResponseEntity<String> resp = rest.exchange("http://localhost:" + port + "/oups", HttpMethod.GET,
				new HttpEntity<>(headers), String.class);
		assertThat(resp).isNotNull();
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(resp.getBody()).isNotNull();

		String body = resp.getBody();
		assertThat(body).contains("Something happened...");
		assertThat(body).contains("An error occurred while processing your request");

		assertThat(body).doesNotContain("Expected: controller used to showcase");
		assertThat(body).doesNotContain("RuntimeException");
		assertThat(body).doesNotContain("java.lang");
		assertThat(body).doesNotContain("at org.springframework");
	}

	@Test
	void testJsonErrorResponseDoesNotExposeExceptionMessage() {
		ResponseEntity<Map<String, Object>> resp = rest.exchange(
				RequestEntity.get("http://localhost:" + port + "/oups").build(),
				new ParameterizedTypeReference<Map<String, Object>>() {
				});
		assertThat(resp).isNotNull();
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(resp.getBody()).isNotNull();

		Map<String, Object> body = resp.getBody();
		assertThat(body).containsKey("timestamp");
		assertThat(body).containsKey("status");
		assertThat(body).containsKey("error");
		assertThat(body).containsKey("path");

		Object message = body.get("message");
		if (message != null) {
			String messageStr = message.toString();
			assertThat(messageStr).doesNotContain("Expected: controller used to showcase");
			assertThat(messageStr).doesNotContain("RuntimeException");
			assertThat(messageStr).doesNotContain("java.lang");
		}
	}

	@Test
	void testErrorPageWithValidInput() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.TEXT_HTML));
		ResponseEntity<String> resp = rest.exchange("http://localhost:" + port + "/", HttpMethod.GET,
				new HttpEntity<>(headers), String.class);
		assertThat(resp).isNotNull();
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(resp.getBody()).isNotNull();
		assertThat(resp.getBody()).contains("Welcome");
	}

	@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class,
			DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
	static class TestConfiguration {

	}

}
