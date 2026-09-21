package product.service;

import exception.business.detailed_exceptions.ProductNameAlreadyInUseException;
import exception.business.detailed_exceptions.InsufficientStockException;
import exception.resource.detailed_exceptions.ProductNotFoundException;
import product.entities.*;
import product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductManagementServiceImpl implements ProductManagementService{
    private final ProductRepository productRepository;
//    constructor
    public ProductManagementServiceImpl(ProductRepository productRepository)
    {
        this.productRepository = productRepository;
    }
//    create new product - need authorize - (default status: INACTIVE) -> activate after
    @Override
    public Product createProduct(ProductName name, int stockQuantity, BigDecimal basePrice){
        Objects.requireNonNull(name,"Product name cannot be null");
        if(stockQuantity < 0){
            throw new IllegalArgumentException("Invalid stock quantity");
        }
        Objects.requireNonNull(basePrice, "Product base price cannot be null");
        if(basePrice.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Product base price has to be bigger than Zero");
        }
        // check if the product is already existing or not
        Optional<Product> product = productRepository.findByName(name);
        if(product.isPresent())
        {
            throw new ProductNameAlreadyInUseException();
        }
        return productRepository.save(
                new Product(name, stockQuantity, basePrice, ProductStatus.INACTIVE)
        );
    }
//    delete product by id
    @Override
    public void deleteProduct(Long productId) {
        Objects.requireNonNull(productId, "Product id cannot be null");
        if (productRepository.findById(productId).isEmpty()) {
            throw new ProductNotFoundException(productId);
        }
        productRepository.deleteById(productId);
    }
//    find product by id
    @Override
    public Product findProductById(Long productId) {
        Objects.requireNonNull(productId,"Product id cannot be null");
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ProductNotFoundException(productId)
                );
        return product;
    }
//    find product by name
    public Product  findProductByName(ProductName productName) {
        Objects.requireNonNull(productName,"Product name cannot be null");
        Product product = productRepository.findByName(productName)
                .orElseThrow(
                        () -> new ProductNotFoundException(productName)
                );
        return product;
    }
//    update product name - need auth
    @Override
    public Product updateProductName(Long productId, ProductName newName) {
        Objects.requireNonNull(productId, "ProductId cannot be null");
        Objects.requireNonNull(newName, "Product name cannot be null");
        // check for existence
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ProductNotFoundException(productId)
                );
        product.changeProductName(newName);
        productRepository.update(product);
        return product;
    }
//    update base price
    @Override
    public Product updateBasePrice(Long productId, BigDecimal newBasePrice) {
        Objects.requireNonNull(productId,"ProductId cannot be null");
        Objects.requireNonNull(newBasePrice,"Product baseprice cannot be null");
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ProductNotFoundException(productId)
                );
        if(product.getBasePrice().compareTo(newBasePrice) == 0) return product;
        product.setBasePrice(newBasePrice);
        productRepository.update(product);
        return product;
    }
//    activate product - need auth
    @Override
    public Product activateProduct(Long productId) {
        Objects.requireNonNull(productId,"Product id cannot be null");
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ProductNotFoundException(productId)
                );
        product.activate();
        productRepository.update(product);
        return product;
    }
//    deactivate product - need auth
    @Override
    public Product deactivateProduct(Long productId)
    {
        Objects.requireNonNull(productId,"Product id cannot be null");
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ProductNotFoundException(productId)
                );
        product.deactivate();
        productRepository.update(product);
        return product;
    }
//    decrease quantity
    @Override
    public Product decreaseStockQuantity(Long productId, int decreaseQuantity) {
        Objects.requireNonNull(productId,"Product id cannot be null");
        if(decreaseQuantity < 0)
        {
            throw new IllegalArgumentException("Product quantity cannot be negative");
        }
        else if(decreaseQuantity == 0) return findProductById(productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return productRepository.decreaseQuantity(productId, decreaseQuantity)
                .orElseThrow(() -> new InsufficientStockException(
                        productId,
                        decreaseQuantity,
                        product.getQuantity()
                ));
    }
//    increase quantity
    @Override
    public Product increaseStockQuantity(Long productId, int increaseQuantity) {
        Objects.requireNonNull(productId,"Product id cannot be null");
        if(increaseQuantity < 0)
        {
            throw new IllegalArgumentException("Product quantity cannot be negative");
        }
        else if(increaseQuantity == 0) return findProductById(productId);
        return productRepository.increaseQuantity(productId, increaseQuantity)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
