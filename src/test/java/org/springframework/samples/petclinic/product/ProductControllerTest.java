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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for ProductController.
 *
 * @author Spring Petclinic Team
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	@Test
	void testRegisterProductSuccess() throws Exception {
		ProductDTO productDTO = new ProductDTO();
		productDTO.setProductName("ドッグフード プレミアム");
		productDTO.setPrice(2500);
		productDTO.setCategory("食事");
		productDTO.setStock(50);

		mockMvc.perform(post("/api/products")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(productDTO)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").exists())
			.andExpect(jsonPath("$.productName", is("ドッグフード プレミアム")))
			.andExpect(jsonPath("$.price", is(2500)))
			.andExpect(jsonPath("$.category", is("食事")))
			.andExpect(jsonPath("$.stock", is(50)));
	}

	@Test
	void testRegisterProductMissingProductName() throws Exception {
		ProductDTO productDTO = new ProductDTO();
		productDTO.setProductName("");
		productDTO.setPrice(2500);
		productDTO.setCategory("食事");
		productDTO.setStock(50);

		mockMvc.perform(post("/api/products")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(productDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error", is("Validation failed")))
			.andExpect(jsonPath("$.details", hasSize(1)))
			.andExpect(jsonPath("$.details[0].field", is("productName")));
	}

	@Test
	void testRegisterProductInvalidPrice() throws Exception {
		ProductDTO productDTO = new ProductDTO();
		productDTO.setProductName("ドッグフード");
		productDTO.setPrice(0);
		productDTO.setCategory("食事");
		productDTO.setStock(50);

		mockMvc.perform(post("/api/products")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(productDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error", is("Validation failed")))
			.andExpect(jsonPath("$.details", hasSize(1)))
			.andExpect(jsonPath("$.details[0].field", is("price")));
	}

	@Test
	void testRegisterProductInvalidStock() throws Exception {
		ProductDTO productDTO = new ProductDTO();
		productDTO.setProductName("ドッグフード");
		productDTO.setPrice(2500);
		productDTO.setCategory("食事");
		productDTO.setStock(-1);

		mockMvc.perform(post("/api/products")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(productDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error", is("Validation failed")))
			.andExpect(jsonPath("$.details", hasSize(1)))
			.andExpect(jsonPath("$.details[0].field", is("stock")));
	}

	@Test
	void testRegisterProductProductNameTooLong() throws Exception {
		ProductDTO productDTO = new ProductDTO();
		productDTO.setProductName("a".repeat(101));
		productDTO.setPrice(2500);
		productDTO.setCategory("食事");
		productDTO.setStock(50);

		mockMvc.perform(post("/api/products")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(productDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error", is("Validation failed")))
			.andExpect(jsonPath("$.details", hasSize(1)))
			.andExpect(jsonPath("$.details[0].field", is("productName")));
	}

	@Test
	void testRegisterProductMultipleValidationErrors() throws Exception {
		ProductDTO productDTO = new ProductDTO();
		productDTO.setProductName("");
		productDTO.setPrice(-100);
		productDTO.setCategory("");
		productDTO.setStock(0);

		mockMvc.perform(post("/api/products")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(productDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error", is("Validation failed")))
			.andExpect(jsonPath("$.details", hasSize(4)));
	}

}
