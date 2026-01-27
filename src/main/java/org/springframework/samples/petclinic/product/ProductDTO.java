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
import jakarta.validation.constraints.Size;

/**
 * DTO for Product request/response.
 *
 * @author Spring Petclinic Team
 */
public class ProductDTO {

	private Integer id;

	@NotBlank(message = "Product name is required")
	@Size(min = 1, max = 100, message = "Product name must be between 1 and 100 characters")
	private String productName;

	@Min(value = 1, message = "Price must be a positive integer")
	private Integer price;

	@NotBlank(message = "Category is required")
	@Size(min = 1, max = 100, message = "Category must be between 1 and 100 characters")
	private String category;

	@Min(value = 1, message = "Stock must be a positive integer")
	private Integer stock;

	public ProductDTO() {
	}

	public ProductDTO(Integer id, String productName, Integer price, String category, Integer stock) {
		this.id = id;
		this.productName = productName;
		this.price = price;
		this.category = category;
		this.stock = stock;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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
