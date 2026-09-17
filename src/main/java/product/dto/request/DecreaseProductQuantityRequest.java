package product.dto.request;

import jakarta.validation.constraints.Positive;

public class DecreaseProductQuantityRequest {
    @Positive(message="The decreased quantity cannot be negative")
    private int decreaseQuantity;
    public int getDecreaseQuantity() {
        return decreaseQuantity;
    }
    public void setDecreaseQuantity(int decreaseQuantity) {
        this.decreaseQuantity = decreaseQuantity;
    }
}
