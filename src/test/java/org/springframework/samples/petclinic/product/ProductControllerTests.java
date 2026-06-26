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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
 * Test class for the {@link ProductController}.
 */
@WebMvcTest(ProductController.class)
@DisabledInNativeImage
@DisabledInAotMode
class ProductControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProductRepository productRepository;

	private Product savedProduct;

	@BeforeEach
	void setup() {
		savedProduct = new Product();
		savedProduct.setId(1);
		savedProduct.setProductName("テスト商品");
		savedProduct.setPrice(1000);
		savedProduct.setCategory("テストカテゴリ");
		savedProduct.setStock(100);

		given(productRepository.save(any(Product.class))).willReturn(savedProduct);
	}

	@Test
	void testCreateProductSuccess() throws Exception {
		String requestBody = """
				{
				    "productName": "テスト商品",
				    "price": 1000,
				    "category": "テストカテゴリ",
				    "stock": 100
				}
				""";

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.productName").value("テスト商品"))
			.andExpect(jsonPath("$.price").value(1000))
			.andExpect(jsonPath("$.category").value("テストカテゴリ"))
			.andExpect(jsonPath("$.stock").value(100));
	}

	@Test
	void testCreateProductWithMaxValues() throws Exception {
		Product maxProduct = new Product();
		maxProduct.setId(2);
		maxProduct.setProductName("A".repeat(100));
		maxProduct.setPrice(1000000);
		maxProduct.setCategory("B".repeat(100));
		maxProduct.setStock(10000);
		given(productRepository.save(any(Product.class))).willReturn(maxProduct);

		String requestBody = """
				{
				    "productName": "%s",
				    "price": 1000000,
				    "category": "%s",
				    "stock": 10000
				}
				""".formatted("A".repeat(100), "B".repeat(100));

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(2))
			.andExpect(jsonPath("$.price").value(1000000))
			.andExpect(jsonPath("$.stock").value(10000));
	}

	@Test
	void testCreateProductMissingProductName() throws Exception {
		String requestBody = """
				{
				    "price": 1000,
				    "category": "テストカテゴリ",
				    "stock": 100
				}
				""";

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testCreateProductBlankProductName() throws Exception {
		String requestBody = """
				{
				    "productName": "   ",
				    "price": 1000,
				    "category": "テストカテゴリ",
				    "stock": 100
				}
				""";

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testCreateProductNegativePrice() throws Exception {
		String requestBody = """
				{
				    "productName": "テスト商品",
				    "price": -1,
				    "category": "テストカテゴリ",
				    "stock": 100
				}
				""";

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testCreateProductPriceExceedsMax() throws Exception {
		String requestBody = """
				{
				    "productName": "テスト商品",
				    "price": 1000001,
				    "category": "テストカテゴリ",
				    "stock": 100
				}
				""";

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testCreateProductStockExceedsMax() throws Exception {
		String requestBody = """
				{
				    "productName": "テスト商品",
				    "price": 1000,
				    "category": "テストカテゴリ",
				    "stock": 10001
				}
				""";

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testCreateProductMissingCategory() throws Exception {
		String requestBody = """
				{
				    "productName": "テスト商品",
				    "price": 1000,
				    "stock": 100
				}
				""";

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testCreateProductMissingStock() throws Exception {
		String requestBody = """
				{
				    "productName": "テスト商品",
				    "price": 1000,
				    "category": "テストカテゴリ"
				}
				""";

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testCreateProductMissingPrice() throws Exception {
		String requestBody = """
				{
				    "productName": "テスト商品",
				    "category": "テストカテゴリ",
				    "stock": 100
				}
				""";

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testCreateProductBlankCategory() throws Exception {
		String requestBody = """
				{
				    "productName": "テスト商品",
				    "price": 1000,
				    "category": "  ",
				    "stock": 100
				}
				""";

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isBadRequest());
	}

}
