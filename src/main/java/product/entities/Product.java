package product.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import product.entities.name.ProductName;
import product.entities.name.ProductNameConverter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_products_name",
                        columnNames = {"name"}
                )
        }
)
@Check(constraints = "btrim(name) <> ''")
@Check(constraints = "quantity >= 0")
@Check(constraints = "price > 0")
@Access(AccessType.FIELD)
public class Product {
    @Id // signed as PRIMARY KEY
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = ProductNameConverter.class)
    @Column(name = "name", nullable = false, length = 255)
    private ProductName name;

    @Column(name = "quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING) // Quyết định cách mà Java Enum sẽ được biểu diễn khi lưu vào database.
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "product_status_enum")
    private ProductStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
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
//    No argument constructor
    protected Product() {}

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
    public Long getId(){return this.id;}
    public ProductName getName(){return this.name;}
    public int getQuantity(){return this.stockQuantity;}
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    public ProductStatus getStatus(){return this.status;}
    public Instant getCreatedAt(){return this.createdAt;}

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
