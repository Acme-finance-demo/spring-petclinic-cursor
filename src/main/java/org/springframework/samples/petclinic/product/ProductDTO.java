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

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for Product.
 *
 * @author Spring Petclinic Team
 */
public class ProductDTO {

	@NotBlank(message = "Product name is required")
	private String productName;

	@Min(value = 0, message = "Price must be 0 or greater")
	private Integer price;

	@NotBlank(message = "Category is required")
	private String category;

	@Min(value = 0, message = "Stock must be 0 or greater")
	private Integer stock;

	public ProductDTO() {
	}

	public ProductDTO(String productName, Integer price, String category, Integer stock) {
		this.productName = productName;
		this.price = price;
		this.category = category;
		this.stock = stock;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

}
