package org.springframework.samples.petclinic.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "商品名は必須です")
    @Size(min = 1, max = 100, message = "商品名は1～100文字である必要があります")
    @Column(nullable = false)
    private String productName;

    @NotNull(message = "価格は必須です")
    @Positive(message = "価格は正の整数である必要があります")
    @Column(nullable = false)
    private Integer price;

    @NotBlank(message = "カテゴリは必須です")
    @Size(min = 1, max = 100, message = "カテゴリは1～100文字である必要があります")
    @Column(nullable = false)
    private String category;

    @NotNull(message = "在庫数は必須です")
    @PositiveOrZero(message = "在庫数は0以上の整数である必要があります")
    @Column(nullable = false)
    private Integer stock;
}
