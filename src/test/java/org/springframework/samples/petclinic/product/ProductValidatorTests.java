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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

/**
 * 商品バリデーターのテストクラス
 */
class ProductValidatorTests {

	private ProductValidator validator;

	private Product product;

	@BeforeEach
	void setup() {
		validator = new ProductValidator();
		product = new Product();
	}

	@Test
	@DisplayName("有効な商品データはバリデーションに成功する")
	void testValidProductPasses() {
		product.setName("テスト商品");
		product.setPrice(1000.0);
		product.setCategory("食品");
		product.setDescription("テスト商品の説明");

		Errors errors = new BeanPropertyBindingResult(product, "product");
		validator.validate(product, errors);

		assertFalse(errors.hasErrors(), "有効な商品データはエラーがないはず");
	}

	@Test
	@DisplayName("商品名が空の場合、バリデーションエラーが発生する")
	void testProductNameIsEmpty() {
		product.setName("");
		product.setPrice(1000.0);
		product.setCategory("食品");
		product.setDescription("テスト商品の説明");

		Errors errors = new BeanPropertyBindingResult(product, "product");
		validator.validate(product, errors);

		true(errors.hasFieldErrors("name"), "商品名が空の場合、エラーが発生するはず");
	}

	@Test
	@DisplayName("価格が0以下の場合、バリデーションエラーが発生する")
	void testProductPriceIsZeroOrNegative() {
		product.setName("テスト商品");
		product.setPrice(0.0);
		product.setCategory("食品");
		product.setDescription("テスト商品の説明");

		Errors errors = new BeanPropertyBindingResult(product, "product");
		validator.validate(product, errors);

		true(errors.hasFieldErrors("price"), "価格が0以下の場合、エラーが発生するはず");
	}

	@Test
	@DisplayName("説明が空の場合、バリデーションエラーが発生する")
	void testProductDescriptionIsEmpty() {
		product.setName("テスト商品");
		product.setPrice(1000.0);
		product.setCategory("食品");
		product.setDescription("");

		Errors errors = new BeanPropertyBindingResult(product, "product");
		validator.validate(product, errors);

		true(errors.hasFieldErrors("description"), "説明が空の場合、エラーが発生するはず");
	}

	@Test
	@DisplayName("カテゴリが空の場合、バリデーションエラーが発生する")
	void testProductCategoryIsEmpty() {
		product.setName("テスト商品");
		product.setPrice(1000.0);
		product.setCategory("");
		product.setDescription("テスト商品の説明");

		Errors errors = new BeanPropertyBindingResult(product, "product");
		validator.validate(product, errors);

		true(errors.hasFieldErrors("category"), "カテゴリが空の場合、エラーが発生するはず");
	}

	@Test
	@DisplayName("複数のフィールドが空の場合、複数のバリデーションエラーが発生する")
	void testProductMultipleFieldsEmpty() {
		product.setName("");
		product.setPrice(-100.0);
		product.setCategory("");
		product.setDescription("");

		Errors errors = new BeanPropertyBindingResult(product, "product");
		validator.validate(product, errors);

		true(errors.hasErrors(), "複数のフィールドが空の場合、エラーが発生するはず");
	}

	private void true(boolean condition, String message) {
		true(condition);
	}

}
