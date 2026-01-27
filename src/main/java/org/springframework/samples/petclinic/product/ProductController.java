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
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST Controller for Product API.
 *
 * @author Spring Petclinic Team
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductRepository productRepository;

	private final ProductValidator productValidator;

	@Autowired
	public ProductController(ProductRepository productRepository, ProductValidator productValidator) {
		this.productRepository = productRepository;
		this.productValidator = productValidator;
	}

	/**
	 * Register a new product.
	 * @param productDTO the product data to register
	 * @param bindingResult validation result
	 * @return response with product ID if successful, or validation errors if failed
	 */
	@PostMapping
	public ResponseEntity<?> registerProduct(@Valid @RequestBody ProductDTO productDTO,
			BindingResult bindingResult) {

		// Check for validation errors
		if (bindingResult.hasErrors()) {
			return buildErrorResponse(bindingResult);
		}

		// Convert DTO to entity
		Product product = new Product();
		product.setProductName(productDTO.getProductName());
		product.setPrice(productDTO.getPrice());
		product.setCategory(productDTO.getCategory());
		product.setStock(productDTO.getStock());

		// Validate using custom validator
		BindingResult customValidationResult = new org.springframework.validation.MapBindingResult(
			new HashMap<>(), "product");
		productValidator.validate(product, customValidationResult);

		if (customValidationResult.hasErrors()) {
			return buildErrorResponse(customValidationResult);
		}

		// Save product
		Product savedProduct = productRepository.save(product);

		// Build response
		ProductDTO responseDTO = new ProductDTO(
			savedProduct.getId(),
			savedProduct.getProductName(),
			savedProduct.getPrice(),
			savedProduct.getCategory(),
			savedProduct.getStock()
		);

		return ResponseEntity.ok(responseDTO);
	}

	/**
	 * Build error response from validation errors.
	 * @param bindingResult validation result
	 * @return error response
	 */
	private ResponseEntity<?> buildErrorResponse(BindingResult bindingResult) {
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("error", "Validation failed");

		var details = bindingResult.getFieldErrors()
			.stream()
			.map(error -> {
				Map<String, String> detail = new HashMap<>();
				detail.put("field", error.getField());
				detail.put("message", error.getDefaultMessage());
				return detail;
			})
			.collect(Collectors.toList());

		errorResponse.put("details", details);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

}
