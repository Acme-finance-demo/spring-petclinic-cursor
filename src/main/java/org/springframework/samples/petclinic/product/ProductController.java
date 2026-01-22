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

	public ProductController(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	/**
	 * Register a new product.
	 * @param productDTO the product data transfer object
	 * @param bindingResult the binding result for validation
	 * @return ResponseEntity with the registered product or error details
	 */
	@PostMapping
	public ResponseEntity<?> registerProduct(@Valid @RequestBody ProductDTO productDTO,
			BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("error", "Validation failed");
			errorResponse.put("details", bindingResult.getFieldErrors().stream()
				.map(error -> {
					Map<String, String> detail = new HashMap<>();
					detail.put("field", error.getField());
					detail.put("message", error.getDefaultMessage());
					return detail;
				})
				.collect(Collectors.toList()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		Product product = new Product();
		product.setName(productDTO.getName());
		product.setPrice(productDTO.getPrice());
		product.setCategory(productDTO.getCategory());
		product.setStock(productDTO.getStock());

		Product savedProduct = productRepository.save(product);

		ProductDTO responseDTO = new ProductDTO(
				savedProduct.getId(),
				savedProduct.getName(),
				savedProduct.getPrice(),
				savedProduct.getCategory(),
				savedProduct.getStock()
		);

		return ResponseEntity.ok(responseDTO);
	}

}
