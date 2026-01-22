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
package org.springframework.samples.petclinic.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for ProductController.
 *
 * @author Demo User
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductRepository productRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void testRegisterProductSuccess() throws Exception {
		Product product = new Product("Test Product", 1000, "Test Category", 50);
		product.setId(1);

		when(productRepository.save(any(Product.class))).thenReturn(product);

		String requestBody = objectMapper.writeValueAsString(product);

		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.name").value("Test Product"))
			.andExpect(jsonPath("$.price").value(1000))
			.andExpect(jsonPath("$.category").value("Test Category"))
			.andExpect(jsonPath("$.stock").value(50));
	}

	@Test
	void testRegisterProductWithInvalidPrice() throws Exception {
		Product product = new Product("Test Product", -100, "Test Category", 50);

		String requestBody = objectMapper.writeValueAsString(product);

		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Validation error"))
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void testRegisterProductWithMissingName() throws Exception {
		String requestBody = "{\"price\": 1000, \"category\": \"Test Category\", \"stock\": 50}";

		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Validation error"))
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void testRegisterProductWithInvalidStock() throws Exception {
		Product product = new Product("Test Product", 1000, "Test Category", 0);

		String requestBody = objectMapper.writeValueAsString(product);

		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Validation error"))
			.andExpect(jsonPath("$.message").exists());
	}

}
