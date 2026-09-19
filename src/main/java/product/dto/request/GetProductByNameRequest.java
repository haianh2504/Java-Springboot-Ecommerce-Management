package product.dto.request;

import jakarta.validation.constraints.NotBlank;

public class GetProductByNameRequest {
    @NotBlank(message = "Product name cannot be blank")
    private String name;
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
