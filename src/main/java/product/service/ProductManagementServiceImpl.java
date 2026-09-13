package product.service;

import exception.business.detailed_exceptions.ProductNameAlreadyInUseException;
import exception.business.detailed_exceptions.InsufficientStockException;
import exception.resource.detailed_exceptions.ProductNotFoundException;
import product.entities.*;
import product.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public class ProductManagementServiceImpl implements ProductManagementService{
    private final ProductRepository productRepository;
//    constructor
    public ProductManagementServiceImpl(ProductRepository productRepository)
    {
        this.productRepository = productRepository;
    }
//    create new physical produc - need authorize - ( default status: UNACTIVE ) -> ACTIVATE after
    public Product createNewPhysicalProduct(ProductName name, int stockQuantity, BigDecimal basePrice, BigDecimal weight){
        Objects.requireNonNull(name,"Product name cannot be null");
        if(stockQuantity < 0){
            throw new IllegalArgumentException("Invalid stock quantity");
        }
        Objects.requireNonNull(basePrice, "Product base price cannot be null");
        if(basePrice.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Product base price has to be bigger than Zero");
        }
        Objects.requireNonNull(weight, "Product weight cannot be null");
        if(weight.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new IllegalArgumentException("Product weight has to be bigger than Zero");
        }
        // check if the product is already existing or not
        Optional<Product> product = productRepository.findByName(name);
        if(product.isPresent())
        {
            throw new ProductNameAlreadyInUseException();
        }
        // new product feature
        ProductType type = ProductType.PHYSICAL;
        ProductStatus status = ProductStatus.INACTIVE;
        // create new
        // save in db
        Product newProduct = productRepository.save(
                new PhysicalProduct(name,stockQuantity,basePrice,status,type,weight)
        );
        return newProduct;
    }

//    create new digital product - need authorize
    @Override
    public Product createNewDigitalProduct(ProductName name, int stockQuantity, BigDecimal basePrice) {
        Objects.requireNonNull(name, "Product name cannot be null");
        if(stockQuantity < 0)
        {
            throw new IllegalArgumentException("Product quantity cannot be negative");
        }
        Objects.requireNonNull(basePrice, "Product base price cannot be null");
        if(basePrice.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new IllegalArgumentException("Product base price has to be bigger than Zero");
        }
        // check if already exist
        Optional<Product> product = productRepository.findByName(name);
        if(product.isPresent()){
            throw new ProductNameAlreadyInUseException();
        }
        Product newProduct = productRepository.save(
                new DigitalProduct(name,stockQuantity,basePrice,ProductStatus.INACTIVE,ProductType.DIGITAL)
        );
        return newProduct;
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
    public void updateProductName(Long productId, ProductName newName) {
        Objects.requireNonNull(productId, "ProductId cannot be null");
        Objects.requireNonNull(newName, "Product name cannot be null");
        // check for existence
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ProductNotFoundException(productId)
                );
        product.changeProductName(newName);
        productRepository.update(product);
    }
//    update product weight - need auth
    @Override
    public void updateProductWeight(Long productId, BigDecimal newWeight) {
        Objects.requireNonNull(productId, "ProductId cannot be null");
        Objects.requireNonNull(newWeight, "Product new weight cannot be null");
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ProductNotFoundException(productId)
                );
        if(product instanceof DigitalProduct)
        {
            throw new IllegalStateException("Digital Product do not have weight");
        }
        if(product instanceof PhysicalProduct physicalProduct)
        {
            if(physicalProduct.getWeight().compareTo(newWeight) == 0) return;
            physicalProduct.setWeight(newWeight);
        }
        productRepository.update(product);
    }
//    update base price
    @Override
    public void updateBasePrice(Long productId, BigDecimal newBasePrice) {
        Objects.requireNonNull(productId,"ProductId cannot be null");
        Objects.requireNonNull(newBasePrice,"Product baseprice cannot be null");
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ProductNotFoundException(productId)
                );
        if(product.getBasePrice().compareTo(newBasePrice) == 0) return;
        product.setBasePrice(newBasePrice);
        productRepository.update(product);
    }
//    activate product - need auth
    @Override
    public void activateProduct(Long productId) {
        Objects.requireNonNull(productId,"Product id cannot be null");
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ProductNotFoundException(productId)
                );
        product.activate();
        productRepository.update(product);
    }
//    deactivate product - need auth
    @Override
    public void deactivateProduct(Long productId)
    {
        Objects.requireNonNull(productId,"Product id cannot be null");
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ProductNotFoundException(productId)
                );
        product.deactivate();
        productRepository.update(product);
    }
//    decrease quantity
    @Override
    public void decreaseStockQuantity(Long productId, int decreaseQuantity) {
        Objects.requireNonNull(productId,"Product id cannot be null");
        if(decreaseQuantity <= 0)
        {
            throw new IllegalArgumentException("Product quantity cannot be negative");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if(!productRepository.decreaseQuantity(productId, decreaseQuantity))
        {
            throw new InsufficientStockException(
                    productId,
                    decreaseQuantity,
                    product.getQuantity()
            );
        }
    }
//    increase quantity
    @Override
    public void increaseStockQuantity(Long productId, int increaseQuantity) {
        Objects.requireNonNull(productId,"Product id cannot be null");
        if(increaseQuantity <= 0)
        {
            throw new IllegalArgumentException("Product quantity cannot be negative");
        }
        if(productRepository.findById(productId).isEmpty()){
            throw new ProductNotFoundException(productId);
        }
        productRepository.increaseQuantity(productId, increaseQuantity);
    }
}
