package product.dto.request;

import jakarta.validation.constraints.Positive;

public class DecreaseProductQuantityRequest {
    @Positive(message="The decreased quantity must be greater than zero")
    private int decreaseQuantity;
    public int getDecreaseQuantity() {
        return decreaseQuantity;
    }
    public void setDecreaseQuantity(int decreaseQuantity) {
        this.decreaseQuantity = decreaseQuantity;
    }
}
