package product.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateProductNameRequest {
    @NotBlank(message="New Product name cannot be blank")
    private String productName;
    public String getName() {
        return this.productName;
    }
    public void setName(String name) {
        this.productName = name;
    }
}
