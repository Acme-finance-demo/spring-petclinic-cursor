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

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.stream.Collectors;

/**
 * REST controller for managing products.
 *
 * @author PetClinic Team
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductRepository productRepository;

	public ProductController(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@PostMapping
	@Transactional
	public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
		Product savedProduct = this.productRepository.save(product);
		return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
		BindingResult bindingResult = ex.getBindingResult();
		String errorMessage = bindingResult.getFieldErrors()
			.stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.collect(Collectors.joining(", "));
		return new ResponseEntity<>(new ErrorResponse(errorMessage), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ AuthenticationException.class, AccessDeniedException.class })
	public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex) {
		return new ResponseEntity<>(new ErrorResponse("Authentication failed: " + ex.getMessage()),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
		return new ResponseEntity<>(new ErrorResponse("Database error: Failed to save product"),
				HttpStatus.SERVICE_UNAVAILABLE);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
		return new ResponseEntity<>(new ErrorResponse("Server error: " + ex.getMessage()),
				HttpStatus.SERVICE_UNAVAILABLE);
	}

}
