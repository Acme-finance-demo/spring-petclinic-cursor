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

	private Integer id;

	@NotBlank(message = "Product name is required")
	private String name;

	@Min(value = 0, message = "Price must be a non-negative integer")
	private Integer price;

	@NotBlank(message = "Category is required")
	private String category;

	@Min(value = 0, message = "Stock must be a non-negative integer")
	private Integer stock;

	public ProductDTO() {
	}

	public ProductDTO(Integer id, String name, Integer price, String category, Integer stock) {
		this.id = id;
		this.name = name;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
