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

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST controller exposing the product registration Web API.
 */
@RestController
@RequestMapping("/api/products")
class ProductController {

	private final ProductRepository products;

	ProductController(ProductRepository products) {
		this.products = products;
	}

	/**
	 * Register a new product. On success the full information of the stored product is
	 * returned together with a {@code 201 Created} status.
	 * @param request the validated product registration request
	 * @return the registered product
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ProductResponse create(@Valid @RequestBody ProductRequest request) {
		Product product = new Product();
		product.setProductName(request.productName());
		product.setPrice(request.price());
		product.setCategory(request.category());
		product.setStock(request.stock());
		return ProductResponse.of(this.products.save(product));
	}

	/**
	 * Translate validation failures into a {@code 400 Bad Request} response carrying the
	 * offending field names and their error messages.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	Map<String, String> handleValidationException(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.put(error.getField(), error.getDefaultMessage());
		}
		return errors;
	}

}
