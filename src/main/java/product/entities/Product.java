package product.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Product {
    private Long id;
    private ProductName name;
    private int stockQuantity;
    private BigDecimal basePrice;
    private ProductStatus status;
    private Instant createdAt;
    public Product(ProductName name, int stockQuantity, BigDecimal basePrice, ProductStatus status)
    {
        this.name = Objects.requireNonNull(name, "Product name cannot be null");
        if(stockQuantity < 0){
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        this.stockQuantity = stockQuantity;
        this.basePrice = Objects.requireNonNull(basePrice,"Product base price cannot be null");
        if(basePrice.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Product base price cannot be negative");
        }
        this.status = Objects.requireNonNull(status,"Product status cannot be null");
        this.createdAt = Instant.now();
    }
//    constructor to return product from database
    public Product(Long id, ProductName name, int stockQuantity, BigDecimal basePrice, ProductStatus status, Instant createdAt)
    {
        this.id = Objects.requireNonNull(id,"productId cannot be null");
        this.name = Objects.requireNonNull(name, "Product name cannot be null");
        if(stockQuantity < 0){
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        this.stockQuantity = stockQuantity;
        this.basePrice = Objects.requireNonNull(basePrice,"Product base price cannot be null");
        if(basePrice.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Product base price cannot be negative");
        }
        this.status = Objects.requireNonNull(status,"Product status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt,"Timestampt createdAt cannot be null");
    }
//    getters
    public final Long getId(){return this.id;}
    public final ProductName getName(){return this.name;}
    public final int getQuantity(){return this.stockQuantity;}
    public final BigDecimal getBasePrice() {
        return basePrice;
    }
    public final ProductStatus getStatus(){return this.status;}
    public final Instant getCreatedAt(){return this.createdAt;}

    //    setters
    public void changeProductName(ProductName name)
    {
        if(name == null)
        {
            throw new NullPointerException("Product Name cannot be null");
        }
        this.name = name;
    }
    public void setStockQuantity(int newQuantity)
    {
        if(newQuantity < 0)
        {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        this.stockQuantity = newQuantity;
    }
    public void setBasePrice(BigDecimal basePrice)
    {
        if(basePrice == null)
        {
            throw new NullPointerException("Product base price cannot be null");
        }
        if(basePrice.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Product base price cannot be negative");
        }
        this.basePrice = basePrice;
    }
//    activate
    public void activate()
    {
        this.status = ProductStatus.ACTIVE;
    }
//    deactivate
    public void deactivate(){
        this.status = ProductStatus.INACTIVE;
    }
//    archive
    public void archive()
    {
        this.status = ProductStatus.ARCHIVED;
    }
}
