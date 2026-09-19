package product.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class UpdateProductBasePriceRequest {
    @NotNull(message="New Base Price cannot be null")
    @Positive(message="New Base Price must be greater than zero")
    private BigDecimal newBasePrice;
    public BigDecimal getNewBasePrice() {
        return newBasePrice;
    }
    public void setNewBasePrice(BigDecimal newBasePrice) {
        this.newBasePrice = newBasePrice;
    }
}
