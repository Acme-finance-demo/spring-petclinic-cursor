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

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 商品バリデーター
 */
@Component
public class ProductValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Product.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Product product = (Product) target;

		if (product.getName() == null || product.getName().isEmpty()) {
			errors.rejectValue("name", "商品名は必須です", "商品名は必須です");
		}

		if (product.getPrice() == null || product.getPrice() <= 0) {
			errors.rejectValue("price", "価格は0より大きい値を入力してください",
				"価格は0より大きい値を入力してください");
		}

		if (product.getDescription() == null || product.getDescription().isEmpty()) {
			errors.rejectValue("description", "商品説明は必須です", "商品説明は必須です");
		}

		if (product.getCategory() == null || product.getCategory().isEmpty()) {
			errors.rejectValue("category", "カテゴリは必須です", "カテゴリは必須です");
		}
	}

}
