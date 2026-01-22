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

import org.springframework.samples.petclinic.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Product entity for product registration.
 *
 * @author Demo User
 */
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

	@NotBlank(message = "Product name is required")
	@Column(name = "name")
	private String name;

	@NotNull(message = "Price is required")
	@Min(value = 1, message = "Price must be a positive integer")
	@Column(name = "price")
	private Integer price;

	@NotBlank(message = "Category is required")
	@Column(name = "category")
	private String category;

	@NotNull(message = "Stock is required")
	@Min(value = 1, message = "Stock must be a positive integer")
	@Column(name = "stock")
	private Integer stock;

	public Product() {
	}

	public Product(String name, Integer price, String category, Integer stock) {
		this.name = name;
		this.price = price;
		this.category = category;
		this.stock = stock;
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
