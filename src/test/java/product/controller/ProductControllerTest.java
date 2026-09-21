package product.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import product.entities.Product;
import product.entities.ProductName;
import product.entities.ProductStatus;
import product.service.ProductManagementService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {
    private static final Long PRODUCT_ID = 1L;
    private static final ProductName PRODUCT_NAME = new ProductName("Mechanical Keyboard");

    @Mock
    private ProductManagementService productManagementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new ProductController(productManagementService)).build();
    }

    private Product product(ProductName name, int quantity, BigDecimal price, ProductStatus status) {
        return new Product(
                PRODUCT_ID,
                name,
                quantity,
                price,
                status,
                Instant.parse("2026-09-10T00:00:00Z")
        );
    }

    private Product persistedProduct() {
        return product(PRODUCT_NAME, 10, new BigDecimal("89.99"), ProductStatus.ACTIVE);
    }

    @Test
    void createProduct_validRequest_returnsCreatedProduct() throws Exception {
        when(productManagementService.createProduct(PRODUCT_NAME, 10, new BigDecimal("89.99")))
                .thenReturn(persistedProduct());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mechanical Keyboard","stockQuantity":10,"basePrice":89.99}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(productManagementService)
                .createProduct(PRODUCT_NAME, 10, new BigDecimal("89.99"));
    }

    @Test
    void getProductById_existingProduct_returnsProduct() throws Exception {
        when(productManagementService.findProductById(PRODUCT_ID)).thenReturn(persistedProduct());

        mockMvc.perform(get("/api/v1/products/{id}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"));

        verify(productManagementService).findProductById(PRODUCT_ID);
    }

    @Test
    void getProductByName_existingProduct_returnsProduct() throws Exception {
        when(productManagementService.findProductByName(PRODUCT_NAME)).thenReturn(persistedProduct());

        mockMvc.perform(get("/api/v1/products").param("name", "Mechanical Keyboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"));

        verify(productManagementService).findProductByName(PRODUCT_NAME);
    }

    @Test
    void searchProducts_validFilters_returnsProductList() throws Exception {
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("100.00");
        when(productManagementService.searchProducts(
                minPrice,
                maxPrice,
                ProductStatus.ACTIVE
        )).thenReturn(List.of(persistedProduct()));

        mockMvc.perform(get("/api/v1/products")
                        .param("minPrice", "50.00")
                        .param("maxPrice", "100.00")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(productManagementService).searchProducts(
                minPrice,
                maxPrice,
                ProductStatus.ACTIVE
        );
    }

    @Test
    void updateProductName_validRequest_returnsUpdatedProduct() throws Exception {
        ProductName newName = new ProductName("Gaming Keyboard");
        Product updated = product(newName, 10, new BigDecimal("89.99"), ProductStatus.ACTIVE);
        when(productManagementService.updateProductName(PRODUCT_ID, newName)).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/products/{id}/name", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Gaming Keyboard\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gaming Keyboard"));

        verify(productManagementService).updateProductName(PRODUCT_ID, newName);
    }

    @Test
    void updateProductBasePrice_validRequest_returnsUpdatedProduct() throws Exception {
        BigDecimal newPrice = new BigDecimal("99.99");
        Product updated = product(PRODUCT_NAME, 10, newPrice, ProductStatus.ACTIVE);
        when(productManagementService.updateBasePrice(PRODUCT_ID, newPrice)).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/products/{id}/base-price", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newBasePrice\":99.99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basePrice").value(99.99));

        verify(productManagementService).updateBasePrice(PRODUCT_ID, newPrice);
    }

    @Test
    void deleteProduct_existingProduct_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", PRODUCT_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productManagementService).deleteProduct(PRODUCT_ID);
    }

    @Test
    void decreaseProductStock_validRequest_returnsUpdatedProduct() throws Exception {
        when(productManagementService.decreaseStockQuantity(PRODUCT_ID, 2))
                .thenReturn(product(PRODUCT_NAME, 8, new BigDecimal("89.99"), ProductStatus.ACTIVE));

        mockMvc.perform(patch("/api/v1/products/{id}/stock/decrease", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decreaseQuantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(8));

        verify(productManagementService).decreaseStockQuantity(PRODUCT_ID, 2);
    }

    @Test
    void increaseProductStock_validRequest_returnsUpdatedProduct() throws Exception {
        when(productManagementService.increaseStockQuantity(PRODUCT_ID, 3))
                .thenReturn(product(PRODUCT_NAME, 13, new BigDecimal("89.99"), ProductStatus.ACTIVE));

        mockMvc.perform(patch("/api/v1/products/{id}/stock/increase", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"increaseQuantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(13));

        verify(productManagementService).increaseStockQuantity(PRODUCT_ID, 3);
    }

    @Test
    void activateProduct_existingProduct_returnsActiveProduct() throws Exception {
        when(productManagementService.activateProduct(PRODUCT_ID)).thenReturn(persistedProduct());

        mockMvc.perform(patch("/api/v1/products/{id}/activation", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(productManagementService).activateProduct(PRODUCT_ID);
    }

    @Test
    void deactivateProduct_existingProduct_returnsInactiveProduct() throws Exception {
        when(productManagementService.deactivateProduct(PRODUCT_ID))
                .thenReturn(product(PRODUCT_NAME, 10, new BigDecimal("89.99"), ProductStatus.INACTIVE));

        mockMvc.perform(patch("/api/v1/products/{id}/deactivation", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(productManagementService).deactivateProduct(PRODUCT_ID);
    }
}
