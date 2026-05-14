/*
 * Copyright 2012-2024 the original author or authors.
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

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 商品コントローラーのテストクラス
 */
@WebMvcTest(ProductController.class)
class ProductControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductRepository productRepository;

	@MockBean
	private ProductValidator productValidator;

	private Product product;

	@BeforeEach
	void setup() {
		product = new Product();
		product.setId(1);
		product.setName("テスト商品");
		product.setPrice(1000.0);
		product.setCategory("食品");
		product.setDescription("テスト商品の説明");
	}

	@Test
	@DisplayName("商品一覧画面が正常に表示される")
	void testShowProductList() throws Exception {
		List<Product> products = new ArrayList<>();
		products.add(product);
		when(productRepository.findAll()).thenReturn(products);

		mockMvc.perform(get("/products"))
			.andExpect(status().isOk())
			.andExpect(view().name("products/productList"))
			.andExpect(model().attributeExists("products"));
	}

	@Test
	@DisplayName("商品作成フォームが正常に表示される")
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/products/new"))
			.andExpect(status().isOk())
			.andExpect(view().name("products/createOrUpdateProductForm"))
			.andExpect(model().attributeExists("product", "categories"));
	}

	@Test
	@DisplayName("商品が正常に作成される")
	void testProcessCreationForm() throws Exception {
		when(productRepository.save(any(Product.class))).thenReturn(product);

		mockMvc.perform(post("/products/new")
			.param("name", "新規商品")
			.param("price", "2000")
			.param("category", "衣類")
			.param("description", "新規商品の説明"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/products"));

		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	@DisplayName("商品編集フォームが正常に表示される")
	void testInitUpdateForm() throws Exception {
		when(productRepository.findById(1)).thenReturn(Optional.of(product));

		mockMvc.perform(get("/products/1/edit"))
			.andExpect(status().isOk())
			.andExpect(view().name("products/createOrUpdateProductForm"))
			.andExpect(model().attributeExists("product", "categories"))
			.andExpect(model().attribute("product", hasProperty("name", is("テスト商品"))));
	}

	@Test
	@DisplayName("商品が正常に更新される")
	void testProcessUpdateForm() throws Exception {
		when(productRepository.save(any(Product.class))).thenReturn(product);

		mockMvc.perform(post("/products/1/edit")
			.param("name", "更新商品")
			.param("price", "3000")
			.param("category", "電子機器")
			.param("description", "更新商品の説明"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/products"));

		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	@DisplayName("商品が正常に削除される")
	void testDeleteProduct() throws Exception {
		doNothing().when(productRepository).deleteById(1);

		mockMvc.perform(post("/products/1/delete"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/products"));

		verify(productRepository, times(1)).deleteById(1);
	}

	@Test
	@DisplayName("存在しない商品の編集フォームにアクセスすると一覧にリダイレクトされる")
	void testInitUpdateFormWithNonExistentProduct() throws Exception {
		when(productRepository.findById(999)).thenReturn(Optional.empty());

		mockMvc.perform(get("/products/999/edit"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/products"));
	}

	@Test
	@DisplayName("バリデーションエラーがある場合、フォームが再表示される")
	void testProcessCreationFormWithValidationError() throws Exception {
		mockMvc.perform(post("/products/new")
			.param("name", "")
			.param("price", "-100")
			.param("category", "")
			.param("description", ""))
			.andExpect(status().isOk())
			.andExpect(view().name("products/createOrUpdateProductForm"))
			.andExpect(model().attributeExists("product", "categories"));
	}

}
