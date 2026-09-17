package product.dto.response;

import product.entities.Product;
import product.entities.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record ProductResponse(
        Long id,
        String name,
        int quantity,
        BigDecimal basePrice,
        ProductStatus status,
        Instant createdAt
) {
    public static ProductResponse from(Product product) {
        Objects.requireNonNull(product, "Product cannot be null");
        return new ProductResponse(
                product.getId(),
                product.getName().name(),
                product.getQuantity(),
                product.getBasePrice(),
                product.getStatus(),
                product.getCreatedAt()
        );
    }
}
