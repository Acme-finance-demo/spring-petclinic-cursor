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

/**
 * Response payload returned after a {@link Product} has been registered, exposing the
 * full information of the stored product including the auto-generated identifier.
 *
 * @param productId the auto-generated product identifier
 * @param productName the product name
 * @param price the price
 * @param category the category
 * @param stock the stock count
 */
public record ProductResponse(Integer productId, String productName, Integer price, String category, Integer stock) {

	static ProductResponse of(Product product) {
		return new ProductResponse(product.getId(), product.getProductName(), product.getPrice(), product.getCategory(),
				product.getStock());
	}

}
