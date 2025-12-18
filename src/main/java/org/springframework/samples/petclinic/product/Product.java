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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.samples.petclinic.model.BaseEntity;

/**
 * 商品エンティティ
 *
 * @author Spring PetClinic Team
 */
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

	@Column(name = "name")
	@Column(name = "name")
	@NotNull(message = "商品名はnullにできません")
	private String name;
	@Column(name = "price")
	@NotNull(message = "価格は必須です")
	@Min(value = 0, message = "価格は0以上である必要があります")
	private Integer price;

	@Column(name = "category")
	@NotBlank(message = "カテゴリは必須です")
	private String category;

	@Column(name = "stock")
	@NotNull(message = "在庫数は必須です")
	@Min(value = 0, message = "在庫数は0以上である必要があります")
	private Integer stock;

	@Column(name = "description")
	private String description;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
