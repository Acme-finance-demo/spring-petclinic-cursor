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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 商品コントローラーの単体テスト
 *
 * @author Spring PetClinic Team
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
		// テストデータの準備
		ProductRequest request = new ProductRequest();
		request.setName("テスト商品");
		request.setPrice(1000);
		request.setCategory("テストカテゴリ");
		request.setStock(10);
		request.setDescription("テスト商品の説明");

		Product savedProduct = new Product();
		savedProduct.setId(1);
		savedProduct.setName(request.getName());
		savedProduct.setPrice(request.getPrice());
		savedProduct.setCategory(request.getCategory());
		savedProduct.setStock(request.getStock());
		savedProduct.setDescription(request.getDescription());

		when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

		// APIリクエストの実行と検証
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("テスト商品"))
				.andExpect(jsonPath("$.price").value(1000))
				.andExpect(jsonPath("$.category").value("テストカテゴリ"))
				.andExpect(jsonPath("$.stock").value(10))
				.andExpect(jsonPath("$.message").value("商品が正常に登録されました"));
	}

	@Test
	void testCreateProduct_ValidationError_MissingName() throws Exception {
		// 商品名が空のリクエスト
		ProductRequest request = new ProductRequest();
		request.setName("");
		request.setPrice(1000);
		request.setCategory("テストカテゴリ");
		request.setStock(10);

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value("error"))
				.andExpect(jsonPath("$.message").value("バリデーションエラーが発生しました"));
	}

	@Test
	void testCreateProduct_ValidationError_NegativePrice() throws Exception {
		// 価格が負の値のリクエスト
		ProductRequest request = new ProductRequest();
		request.setName("テスト商品");
		request.setPrice(-100);
		request.setCategory("テストカテゴリ");
		request.setStock(10);

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value("error"));
	}

	@Test
	void testCreateProduct_ValidationError_NullStock() throws Exception {
		// 在庫数がnullのリクエスト
		ProductRequest request = new ProductRequest();
		request.setName("テスト商品");
		request.setPrice(1000);
		request.setCategory("テストカテゴリ");
		request.setStock(null);

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value("error"));
	}

}
