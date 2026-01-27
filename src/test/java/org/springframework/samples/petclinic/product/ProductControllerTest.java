package org.springframework.samples.petclinic.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setProductName("テスト商品");
        testProduct.setPrice(1000);
        testProduct.setCategory("テストカテゴリ");
        testProduct.setStock(10);
    }

    @Test
    @WithMockUser
    void testCreateProductSuccess() throws Exception {
        when(productService.createProduct(any(Product.class)))
                .thenReturn(testProduct);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productName").value("テスト商品"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.category").value("テストカテゴリ"))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void testCreateProductWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("認証が必要です"));
    }

    @Test
    @WithMockUser
    void testCreateProductWithInvalidProductName() throws Exception {
        Product invalidProduct = new Product();
        invalidProduct.setProductName("");
        invalidProduct.setPrice(1000);
        invalidProduct.setCategory("テストカテゴリ");
        invalidProduct.setStock(10);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("バリデーションエラー"));
    }

    @Test
    @WithMockUser
    void testCreateProductWithNegativePrice() throws Exception {
        Product invalidProduct = new Product();
        invalidProduct.setProductName("テスト商品");
        invalidProduct.setPrice(-100);
        invalidProduct.setCategory("テストカテゴリ");
        invalidProduct.setStock(10);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("バリデーションエラー"));
    }

    @Test
    @WithMockUser
    void testCreateProductWithNegativeStock() throws Exception {
        Product invalidProduct = new Product();
        invalidProduct.setProductName("テスト商品");
        invalidProduct.setPrice(1000);
        invalidProduct.setCategory("テストカテゴリ");
        invalidProduct.setStock(-5);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("バリデーションエラー"));
    }
}
