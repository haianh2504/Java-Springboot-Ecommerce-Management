package product.repository;

import product.entities.Product;
import product.entities.ProductName;
import product.entities.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
//    Save Product
    public Product save(Product product);
//    find product by Id
    public Optional<Product> findById(Long productId);
//    find product by name
    public Optional<Product> findByName(ProductName name);
//    search products by optional price range and status
    public List<Product> search(BigDecimal minPrice, BigDecimal maxPrice, ProductStatus status);
//    delete product by id
    public void deleteById(Long productId);
//    update product after changes
    public void update(Product product);
//    decrease quantity by number of..
    public Optional<Product> decreaseQuantity(Long productId, int quantity);
//    increase quantity of number of..
    public Optional<Product> increaseQuantity(Long productId, int quantity);
}
