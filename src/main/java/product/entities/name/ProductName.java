package product.entities.name;

import java.util.Objects;

public record ProductName(String name) {
    public ProductName{
        Objects.requireNonNull(name, "ProductName cannot be null");
        name = name.trim();
        if(name.isEmpty()){
            throw new IllegalArgumentException("ProductName cannot be empty");
        }
    }
}
