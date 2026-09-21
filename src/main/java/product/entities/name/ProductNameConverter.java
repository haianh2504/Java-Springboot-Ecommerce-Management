package product.entities.name;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProductNameConverter implements AttributeConverter<ProductName, String> {
    @Override
    public String convertToDatabaseColumn(ProductName productName) {
        return productName == null ? null : productName.name();
    }
    @Override
    public ProductName convertToEntityAttribute(String productName) {
        return productName == null ? null : new ProductName(productName);
    }
}
