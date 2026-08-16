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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for registering a new {@link Product} through the Web API.
 *
 * @param productName the product name (1-100 characters, blank values are rejected)
 * @param price the price (0 or greater, up to 1,000,000)
 * @param category the category (1-100 characters, blank values are rejected)
 * @param stock the stock count (0 or greater, up to 10,000)
 */
public record ProductRequest(

		@NotBlank @Size(min = 1, max = 100) String productName,

		@NotNull @Min(0) @Max(1_000_000) Integer price,

		@NotBlank @Size(min = 1, max = 100) String category,

		@NotNull @Min(0) @Max(10_000) Integer stock) {
}
