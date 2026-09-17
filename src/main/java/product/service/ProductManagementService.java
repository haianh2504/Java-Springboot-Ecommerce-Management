package product.service;

import product.entities.Product;
import product.entities.ProductName;

import java.math.BigDecimal;

public interface ProductManagementService {
//    create new product
    public Product createProduct(ProductName name, int stockQuantity, BigDecimal basePrice);
//    delete a product

//    find Product by id
    public Product findProductById(Long productId);

//    find Product by name
    public Product findProductByName(ProductName name);

//    update product name - need authorize
    public void updateProductName(Long productId, ProductName newName);

//    update base price - need autho
    public void updateBasePrice(Long productId, BigDecimal newBasePrice);

//    decrease stock quantity
    public void decreaseStockQuantity(Long productId, int decreaseQuantity);

//    increase stock quantity
    public void increaseStockQuantity(Long productId, int increaseQuantity);

//    activate product - need autho
    public void activateProduct(Long productId);

//    deactivate product - need autho
    public void deactivateProduct(Long productId);
}
