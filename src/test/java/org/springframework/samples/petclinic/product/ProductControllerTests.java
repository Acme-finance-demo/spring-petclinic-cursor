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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 商品コントローラーのテスト
 */
@WebMvcTest(ProductController.class)
class ProductControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductRepository productRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void testCreateProduct_Success() throws Exception {
		// テストデータ準備
		Product product = new Product();
		product.setName("テスト商品");
		product.setPrice(new BigDecimal("1000"));
		product.setCategory("テストカテゴリ");
		product.setStock(10);
		product.setDescription("テスト商品の説明");

		Product savedProduct = new Product();
		savedProduct.setId(1);
		savedProduct.setName(product.getName());
		savedProduct.setPrice(product.getPrice());
		savedProduct.setCategory(product.getCategory());
		savedProduct.setStock(product.getStock());
		savedProduct.setDescription(product.getDescription());

		when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

		// APIリクエスト実行
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(product)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.productId").value(1));
	}

	@Test
	void testCreateProduct_ValidationError_MissingName() throws Exception {
		// 商品名が空のテストデータ
		Product product = new Product();
		product.setPrice(new BigDecimal("1000"));
		product.setCategory("テストカテゴリ");
		product.setStock(10);

		// APIリクエスト実行
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(product)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("error"));
	}

	@Test
	void testCreateProduct_ValidationError_NegativePrice() throws Exception {
		// 負の価格のテストデータ
		Product product = new Product();
		product.setName("テスト商品");
		product.setPrice(new BigDecimal("-100"));
		product.setCategory("テストカテゴリ");
		product.setStock(10);

		// APIリクエスト実行
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(product)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("error"));
	}

	@Test
	void testCreateProduct_ValidationError_NegativeStock() throws Exception {
		// 負の在庫数のテストデータ
		Product product = new Product();
		product.setName("テスト商品");
		product.setPrice(new BigDecimal("1000"));
		product.setCategory("テストカテゴリ");
		product.setStock(-5);

		// APIリクエスト実行
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(product)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("error"));
	}

	@Test
	void testCreateProduct_ValidationError_EmptyName() throws Exception {
		// 商品名が空文字のテストデータ
		Product product = new Product();
		product.setName("");
		product.setPrice(new BigDecimal("1000"));
		product.setCategory("テストカテゴリ");
		product.setStock(10);

		// APIリクエスト実行
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(product)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("error"));
	}

}
