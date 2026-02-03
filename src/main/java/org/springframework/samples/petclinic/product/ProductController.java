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

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 商品コントローラー
 */
@Controller
@RequestMapping("/products")
public class ProductController {

	private final ProductRepository productRepository;

	private final ProductValidator productValidator;

	public ProductController(ProductRepository productRepository, ProductValidator productValidator) {
		this.productRepository = productRepository;
		this.productValidator = productValidator;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
		dataBinder.addValidators(productValidator);
	}

	/**
	 * 商品一覧を表示
	 */
	@GetMapping
	public String showProductList(Model model) {
		model.addAttribute("products", productRepository.findAll());
		return "products/productList";
	}

	/**
	 * 商品作成フォームを表示
	 */
	@GetMapping("/new")
	public String initCreationForm(Model model) {
		Product product = new Product();
		model.addAttribute("product", product);
		model.addAttribute("categories", getCategories());
		return "products/createOrUpdateProductForm";
	}

	/**
	 * 商品を作成
	 */
	@PostMapping("/new")
	public String processCreationForm(Product product, BindingResult result, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("categories", getCategories());
			return "products/createOrUpdateProductForm";
		}
		this.productRepository.save(product);
		return "redirect:/products";
	}

	/**
	 * 商品編集フォームを表示
	 */
	@GetMapping("/{productId}/edit")
	public String initUpdateForm(@PathVariable("productId") int productId, Model model) {
		Product product = this.productRepository.findById(productId).orElse(null);
		if (product == null) {
			return "redirect:/products";
		}
		model.addAttribute("product", product);
		model.addAttribute("categories", getCategories());
		return "products/createOrUpdateProductForm";
	}

	/**
	 * 商品を更新
	 */
	@PostMapping("/{productId}/edit")
	public String processUpdateForm(Product product, BindingResult result,
		@PathVariable("productId") int productId, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("categories", getCategories());
			return "products/createOrUpdateProductForm";
		}
		product.setId(productId);
		this.productRepository.save(product);
		return "redirect:/products";
	}

	/**
	 * 商品を削除
	 */
	@PostMapping("/{productId}/delete")
	public String deleteProduct(@PathVariable("productId") int productId) {
		this.productRepository.deleteById(productId);
		return "redirect:/products";
	}

	/**
	 * カテゴリのマップを取得
	 */
	private Map<String, String> getCategories() {
		return Map.of(
			"食品", "食品",
			"衣類", "衣類",
			"電子機器", "電子機器",
			"その他", "その他"
		);
	}

}
