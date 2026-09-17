package product.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class UpdateProductBasePriceRequest {
    @PositiveOrZero(message="New Base Price cannot be negative or equals Zero")
    private BigDecimal newBasePrice;
    public BigDecimal getNewBasePrice() {
        return newBasePrice;
    }
    public void setNewBasePrice(BigDecimal newBasePrice) {
        this.newBasePrice = newBasePrice;
    }
}
