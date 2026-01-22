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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for ProductController.
 *
 * @author Spring Petclinic Team
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@WithMockUser
	void testCreateProductSuccess() throws Exception {
		ProductDTO productDTO = new ProductDTO("ドッグフード プレミアム", 2500, "食事", 100);

		mockMvc.perform(post("/api/products")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(productDTO)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").exists())
			.andExpect(jsonPath("$.productName").value("ドッグフード プレミアム"))
			.andExpect(jsonPath("$.price").value(2500))
			.andExpect(jsonPath("$.category").value("食事"))
			.andExpect(jsonPath("$.stock").value(100));
	}

	@Test
	@WithMockUser
	void testCreateProductValidationError() throws Exception {
		ProductDTO productDTO = new ProductDTO("", -100, "食事", 100);

		mockMvc.perform(post("/api/products")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(productDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Validation failed"))
			.andExpect(jsonPath("$.details").isArray());
	}

	@Test
	@WithMockUser
	void testCreateProductNegativeStock() throws Exception {
		ProductDTO productDTO = new ProductDTO("ドッグフード", 2500, "食事", -10);

		mockMvc.perform(post("/api/products")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(productDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Validation failed"));
	}

	@Test
	void testCreateProductUnauthorized() throws Exception {
		ProductDTO productDTO = new ProductDTO("ドッグフード プレミアム", 2500, "食事", 100);

		mockMvc.perform(post("/api/products")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(productDTO)))
			.andExpect(status().isUnauthorized());
	}

}
