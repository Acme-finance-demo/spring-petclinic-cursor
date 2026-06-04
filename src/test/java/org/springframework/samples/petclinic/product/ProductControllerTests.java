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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for {@link ProductController}
 *
 * @author PetClinic Team
 */
@WebMvcTest(ProductController.class)
@DisabledInNativeImage
@DisabledInAotMode
class ProductControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProductRepository productRepository;

	@Test
	void testCreateProductSuccess() throws Exception {
		Product product = new Product();
		product.setId(1);
		product.setProductName("Test Product");
		product.setPrice(5000);
		product.setCategory("Electronics");
		product.setStock(100);

		given(this.productRepository.save(any(Product.class))).willReturn(product);

		mockMvc
			.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content("{\"productName\":\"Test Product\",\"price\":5000,\"category\":\"Electronics\",\"stock\":100}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.productName").value("Test Product"))
			.andExpect(jsonPath("$.price").value(5000))
			.andExpect(jsonPath("$.category").value("Electronics"))
			.andExpect(jsonPath("$.stock").value(100));
	}

	@Test
	void testCreateProductWithEmptyProductName() throws Exception {
		mockMvc
			.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content("{\"productName\":\"\",\"price\":5000,\"category\":\"Electronics\",\"stock\":100}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void testCreateProductWithBlankProductName() throws Exception {
		mockMvc
			.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content("{\"productName\":\"   \",\"price\":5000,\"category\":\"Electronics\",\"stock\":100}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void testCreateProductWithNegativePrice() throws Exception {
		mockMvc
			.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content("{\"productName\":\"Test Product\",\"price\":-100,\"category\":\"Electronics\",\"stock\":100}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void testCreateProductWithZeroPrice() throws Exception {
		mockMvc
			.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content("{\"productName\":\"Test Product\",\"price\":0,\"category\":\"Electronics\",\"stock\":100}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void testCreateProductWithPriceExceedingMax() throws Exception {
		mockMvc
			.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"productName\":\"Test Product\",\"price\":1000001,\"category\":\"Electronics\",\"stock\":100}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void testCreateProductWithStockExceedingMax() throws Exception {
		mockMvc
			.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"productName\":\"Test Product\",\"price\":5000,\"category\":\"Electronics\",\"stock\":10001}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void testCreateProductWithNegativeStock() throws Exception {
		mockMvc
			.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"productName\":\"Test Product\",\"price\":5000,\"category\":\"Electronics\",\"stock\":-1}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void testCreateProductWithEmptyCategory() throws Exception {
		mockMvc
			.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content("{\"productName\":\"Test Product\",\"price\":5000,\"category\":\"\",\"stock\":100}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void testCreateProductWithDatabaseError() throws Exception {
		given(this.productRepository.save(any(Product.class)))
			.willThrow(new DataAccessException("Database connection failed") {
			});

		mockMvc
			.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content("{\"productName\":\"Test Product\",\"price\":5000,\"category\":\"Electronics\",\"stock\":100}"))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void testCreateProductWithMaxValidPrice() throws Exception {
		Product product = new Product();
		product.setId(1);
		product.setProductName("Expensive Product");
		product.setPrice(1000000);
		product.setCategory("Luxury");
		product.setStock(1);

		given(this.productRepository.save(any(Product.class))).willReturn(product);

		mockMvc
			.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"productName\":\"Expensive Product\",\"price\":1000000,\"category\":\"Luxury\",\"stock\":1}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.price").value(1000000));
	}

	@Test
	void testCreateProductWithMaxValidStock() throws Exception {
		Product product = new Product();
		product.setId(1);
		product.setProductName("High Stock Product");
		product.setPrice(100);
		product.setCategory("Wholesale");
		product.setStock(10000);

		given(this.productRepository.save(any(Product.class))).willReturn(product);

		mockMvc
			.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"productName\":\"High Stock Product\",\"price\":100,\"category\":\"Wholesale\",\"stock\":10000}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.stock").value(10000));
	}

}
