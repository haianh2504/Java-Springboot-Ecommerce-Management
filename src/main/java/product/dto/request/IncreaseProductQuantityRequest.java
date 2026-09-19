package product.dto.request;

import jakarta.validation.constraints.Positive;


public class IncreaseProductQuantityRequest {
    @Positive(message="The increased quantity must be greater than zero")
    private int increaseQuantity;
    public int getIncreaseQuantity() {
        return increaseQuantity;
    }
    public void setIncreaseQuantity(int increaseQuantity) {
        this.increaseQuantity = increaseQuantity;
    }
}
