package product.service;

import product.entities.Product;
import product.entities.ProductName;

import java.math.BigDecimal;

public interface ProductManagementService {
//    create new product
    public Product createProduct(ProductName name, int stockQuantity, BigDecimal basePrice);

//    delete a product
    public void deleteProduct(Long productId);

//    find Product by id
    public Product findProductById(Long productId);

//    find Product by name
    public Product findProductByName(ProductName name);

//    update product name - need authorize
    public Product updateProductName(Long productId, ProductName newName);

//    update base price - need autho
    public Product updateBasePrice(Long productId, BigDecimal newBasePrice);

//    decrease stock quantity
    public Product decreaseStockQuantity(Long productId, int decreaseQuantity);

//    increase stock quantity
    public Product increaseStockQuantity(Long productId, int increaseQuantity);

//    activate product - need autho
    public Product activateProduct(Long productId);

//    deactivate product - need autho
    public Product deactivateProduct(Long productId);
}
