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

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品登録WebAPIコントローラー
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductRepository productRepository;

	public ProductController(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	/**
	 * 商品登録API
	 * @param product 登録する商品情報
	 * @param result バリデーション結果
	 * @return 登録された商品のIDを含むレスポンス
	 */
	@PostMapping
	public ResponseEntity<?> createProduct(@Valid @RequestBody Product product, BindingResult result) {
		// バリデーションエラーチェック
		if (result.hasErrors()) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("status", "error");
			errorResponse.put("message", "バリデーションエラーが発生しました");
			errorResponse.put("errors",
					result.getFieldErrors()
						.stream()
						.collect(Collectors.toMap(error -> error.getField(), error -> error.getDefaultMessage())));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		// 商品を保存
		Product savedProduct = productRepository.save(product);

		// 成功レスポンス
		Map<String, Object> response = new HashMap<>();
		response.put("status", "success");
		response.put("message", "商品が正常に登録されました");
		response.put("productId", savedProduct.getId());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

}
