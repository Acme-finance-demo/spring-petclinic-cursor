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

import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.petclinic.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Simple JavaBean domain object representing a product.
 *
 * @author Spring Petclinic Team
 */
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

	@Column(name = "product_name")
	@NotBlank(message = "Product name is required")
	@Size(min = 1, max = 100, message = "Product name must be between 1 and 100 characters")
	private String productName;

	@Column(name = "price")
	@Min(value = 1, message = "Price must be a positive integer")
	private Integer price;

	@Column(name = "category")
	@NotBlank(message = "Category is required")
	@Size(min = 1, max = 100, message = "Category must be between 1 and 100 characters")
	private String category;

	@Column(name = "stock")
	@Min(value = 1, message = "Stock must be a positive integer")
	private Integer stock;

	public String getProductName() {
		return this.productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Integer getPrice() {
		return this.price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public String getCategory() {
		return this.category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getStock() {
		return this.stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId())
			.append("productName", this.productName)
			.append("price", this.price)
			.append("category", this.category)
			.append("stock", this.stock)
			.toString();
	}

}
