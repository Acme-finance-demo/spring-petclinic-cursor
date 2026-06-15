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
 * 商品API コントローラー
 * 商品登録機能を提供するREST APIエンドポイント
 *
 * @author Spring PetClinic Team
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
	 * POST /api/products
	 *
	 * @param request 商品登録リクエスト
	 * @param result バリデーション結果
	 * @return 登録された商品情報またはエラーレスポンス
	 */
	@PostMapping
	public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest request, BindingResult result) {
		// バリデーションエラーチェック
		if (result.hasErrors()) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("status", "error");
			errorResponse.put("message", "バリデーションエラーが発生しました");
			errorResponse.put("errors", result.getFieldErrors().stream()
					.map(error -> Map.of("field", error.getField(), "message", error.getDefaultMessage()))
					.collect(Collectors.toList()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		// Productエンティティの作成と保存
		Product product = new Product();
		product.setName(request.getName());
		product.setPrice(request.getPrice());
		product.setCategory(request.getCategory());
		product.setStock(request.getStock());
		product.setDescription(request.getDescription());

		Product savedProduct = productRepository.save(product);

		// レスポンスの作成
		ProductResponse response = new ProductResponse(savedProduct, "商品が正常に登録されました");
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

}
