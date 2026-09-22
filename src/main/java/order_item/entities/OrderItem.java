package order_item.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(
        name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_order_items_order_product",
                        columnNames = {"order_id", "product_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_order_items_product_id",
                        columnList = "product_id"
                )
        }
)
@Check(
        name = "ck_order_items_quantity_positive",
        constraints = "quantity > 0"
)
@Check(
        name = "ck_order_items_unit_price_positive",
        constraints = "unit_price > 0"
)
@Access(AccessType.FIELD)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id",
            nullable = false
    )
    private Long orderItemId;

    @Column(
            name = "order_id",
            nullable = false,
            updatable = false
    )
    private Long orderId;

    @Column(
            name = "product_id",
            nullable = false,
            updatable = false
    )
    private Long productId;

    @Column(
            name = "quantity",
            nullable = false
    )
    private int quantity;

    @Column(
            name = "unit_price",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal unitPrice;
    // No-argument constructor required by JPA
    protected OrderItem() {}
//    constructor for SQL return
    public OrderItem(Long orderItemId, Long orderId, Long productId, int quantity, BigDecimal unitPrice)
    {
        this.orderItemId = Objects.requireNonNull(orderItemId, "orderItemId cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "orderId cannot be null");
        this.productId = Objects.requireNonNull(productId, "ProductID cannot be null");
        if(quantity <= 0) throw new IllegalArgumentException("Invalid quantity");
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice, "UnitPrice cannot be null");
        if(unitPrice.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Invalid UnitPrice");
    }
//    constructor for creating one
public OrderItem(Long orderId, Long productId, int quantity, BigDecimal unitPrice)
{
    this.orderId = Objects.requireNonNull(orderId, "orderId cannot be null");
    this.productId = Objects.requireNonNull(productId, "ProductID cannot be null");
    if(quantity <= 0) throw new IllegalArgumentException("Invalid quantity");
    this.quantity = quantity;
    this.unitPrice = Objects.requireNonNull(unitPrice, "UnitPrice cannot be null");
    if(unitPrice.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Invalid UnitPrice");
}

//    getters
    public final Long getOrderItemId() {return this.orderItemId;}
    public final Long getOrderId() {return this.orderId;}
    public final Long getProductId() {
        return this.productId;
    }
    public final int getQuantity() {
        return quantity;
    }
    public final BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
