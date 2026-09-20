package product.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import product.dto.request.*;
import product.dto.response.ProductResponse;
import product.entities.Product;
import product.entities.ProductName;
import product.service.ProductManagementService;

@RestController
@RequestMapping("/api/v1/products")
public final class ProductController {
    private final ProductManagementService productManagementService;
    // constructor
    public ProductController(ProductManagementService productManagementService) {
        this.productManagementService = productManagementService;
    }
    // CREATE product
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody @Valid CreateProductRequest request
    ) {
        Product newProduct = productManagementService.createProduct(
                new ProductName(request.getName()),
                request.getStockQuantity(),
                request.getBasePrice()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductResponse.from(newProduct));
    }

    // GET product by Id
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") @Positive Long productId)
    {
        Product savedProduct = productManagementService.findProductById(productId);
        return ResponseEntity.ok(ProductResponse.from(savedProduct));
    }

    // GET product by name
    @GetMapping(params = "name")
    public ResponseEntity<ProductResponse> getProductByName(
            @RequestParam @NotNull String name
            )
    {
        Product persistedProduct = productManagementService.findProductByName(
                new ProductName(name)
        );
        return ResponseEntity.ok(ProductResponse.from(persistedProduct));
    }

    // UPDATE product name
    @PatchMapping("/{id}/name")
    public ResponseEntity<ProductResponse> updateProductNameById(
            @PathVariable @Positive Long id,
            @RequestBody @Valid UpdateProductNameRequest request
    )
    {
        Product product = productManagementService.updateProductName(
                id,
                new ProductName(request.getName())
        );
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    // UPDATE product base price
    @PatchMapping("/{id}/base-price")
    public ResponseEntity<ProductResponse> updateProductBasePrice(
            @PathVariable @Positive Long id,
            @RequestBody @Valid UpdateProductBasePriceRequest request
    ) {
        Product product = productManagementService.updateBasePrice(
                id,
                request.getNewBasePrice()
        );
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    // DELETE by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(
            @PathVariable @Positive Long id
    )
    {
        productManagementService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // DECREASE product stock
    @PatchMapping("/{id}/stock/decrease")
    public ResponseEntity<ProductResponse> decreaseProductStock(
            @PathVariable @Positive Long id,
            @RequestBody @Valid DecreaseProductQuantityRequest request
    ) {
        Product product = productManagementService.decreaseStockQuantity(
                id,
                request.getDecreaseQuantity()
        );
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    // INCREASE product stock
    @PatchMapping("/{id}/stock/increase")
    public ResponseEntity<ProductResponse> increaseProductStock(
            @PathVariable @Positive Long id,
            @RequestBody @Valid IncreaseProductQuantityRequest request
    ) {
        Product product = productManagementService.increaseStockQuantity(
                id,
                request.getIncreaseQuantity()
        );
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    // ACTIVATE product
    @PatchMapping("/{id}/activation")
    public ResponseEntity<ProductResponse> activateProduct(
            @PathVariable @Positive Long id
    ) {
        Product product = productManagementService.activateProduct(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    // DEACTIVATE product
    @PatchMapping("/{id}/deactivation")
    public ResponseEntity<ProductResponse> deactivateProduct(
            @PathVariable @Positive Long id
    ) {
        Product product = productManagementService.deactivateProduct(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }
}
