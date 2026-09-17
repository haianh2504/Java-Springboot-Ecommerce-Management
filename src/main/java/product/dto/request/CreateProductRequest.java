package product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProductRequest {
    @NotNull(message = "Product name cannot be null")
    @NotBlank(message="Product name cannot be blank")
    private String name;

    @PositiveOrZero(message="Stock quantity cannot be negative")
    private int stockQuantity;

    @NotNull(message="Base Price cannot be null")
    @Positive(message = "Base Price cannot be negative")
    private BigDecimal basePrice;
}
