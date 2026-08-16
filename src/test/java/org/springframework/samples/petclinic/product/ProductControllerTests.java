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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link ProductController}.
 */
@WebMvcTest(ProductController.class)
@DisabledInNativeImage
@DisabledInAotMode
class ProductControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProductRepository products;

	@Test
	void testCreateProductSuccess() throws Exception {
		given(this.products.save(any(Product.class))).willAnswer(invocation -> {
			Product saved = invocation.getArgument(0);
			saved.setId(1);
			return saved;
		});

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("""
				{"productName":"Dog Food","price":1500,"category":"Food","stock":100}"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.productId").value(1))
			.andExpect(jsonPath("$.productName").value("Dog Food"))
			.andExpect(jsonPath("$.price").value(1500))
			.andExpect(jsonPath("$.category").value("Food"))
			.andExpect(jsonPath("$.stock").value(100));
	}

	@Test
	void testCreateProductWithBlankNameReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("""
				{"productName":"   ","price":1500,"category":"Food","stock":100}"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.productName").exists());
	}

	@Test
	void testCreateProductWithNegativePriceReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("""
				{"productName":"Dog Food","price":-1,"category":"Food","stock":100}"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.price").exists());
	}

	@Test
	void testCreateProductWithPriceAboveLimitReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("""
				{"productName":"Dog Food","price":1000001,"category":"Food","stock":100}"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.price").exists());
	}

	@Test
	void testCreateProductWithStockAboveLimitReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("""
				{"productName":"Dog Food","price":1500,"category":"Food","stock":10001}"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.stock").exists());
	}

	@Test
	void testCreateProductWithMissingFieldReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("""
				{"productName":"Dog Food","category":"Food","stock":100}"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.price").exists());
	}

}
