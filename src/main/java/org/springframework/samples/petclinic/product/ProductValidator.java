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

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for Product.
 *
 * @author Spring Petclinic Team
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

		if (product.getProductName() == null || product.getProductName().isBlank()) {
			errors.rejectValue("productName", "required", "Product name is required");
		} else if (product.getProductName().length() > 100) {
			errors.rejectValue("productName", "size",
				"Product name must be between 1 and 100 characters");
		}

		if (product.getPrice() == null || product.getPrice() <= 0) {
			errors.rejectValue("price", "required", "Price must be a positive integer");
		}

		if (product.getCategory() == null || product.getCategory().isBlank()) {
			errors.rejectValue("category", "required", "Category is required");
		} else if (product.getCategory().length() > 100) {
			errors.rejectValue("category", "size",
				"Category must be between 1 and 100 characters");
		}

		if (product.getStock() == null || product.getStock() <= 0) {
			errors.rejectValue("stock", "required", "Stock must be a positive integer");
		}
	}

}
