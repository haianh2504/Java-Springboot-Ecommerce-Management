package order.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_orders_cart",
                        columnNames = {"cart_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_orders_user_created_at",
                        columnList = "user_id, created_at DESC"
                ),
                @Index(
                        name = "idx_orders_status_created_at",
                        columnList = "status, created_at DESC"
                )
        }
)
@Check(
        name = "ck_orders_sub_total_positive",
        constraints = "sub_total > 0"
)
@Check(
        name = "ck_orders_shipping_fee_non_negative",
        constraints = "shipping_fee >= 0"
)
@Check(
        name = "ck_orders_discount_non_negative",
        constraints = "discount_amount >= 0"
)
@Check(
        name = "ck_orders_discount_not_too_large",
        constraints = "discount_amount <= sub_total + shipping_fee"
)
@Check(
        name = "ck_orders_total_price_non_negative",
        constraints = "total_price >= 0"
)
@Check(
        name = "ck_orders_total_price_correct",
        constraints = "total_price = sub_total + shipping_fee - discount_amount"
)
@Access(AccessType.FIELD)
public class Order {
    // identity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long orderId;

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Column(name = "user_id", nullable = false)
    private Long userId;


    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "order_status_enum")
    private OrderStatus orderStatus; // initially PENDING

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // financial
    @Column(name = "sub_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal subTotal; // total final price of all product

    @Column(name = "shipping_fee", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount; // discount = 0 initially

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice; // total price + shippingFee + discountAmount

//    No argu constructor for JPA management
    protected Order() {
    }

//    constructor - SQL return
    public Order(
            Long orderId,
            Long userId,
            Long cartId,
            OrderStatus orderStatus,
            Instant createdAt,
            BigDecimal subTotal,
            BigDecimal shippingFee,
            BigDecimal discountAmount,
            BigDecimal totalPrice
    ) {
        this.orderId = Objects.requireNonNull(orderId, "orderId cannot be null");
        this.cartId = Objects.requireNonNull(cartId, "cartId cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.orderStatus = Objects.requireNonNull(orderStatus, "orderStatus cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.subTotal = Objects.requireNonNull(subTotal, "subTotal cannot be null");
        this.shippingFee = (shippingFee == null) ? BigDecimal.ZERO : shippingFee;
        this.discountAmount = (discountAmount == null) ? BigDecimal.ZERO : discountAmount;
        this.totalPrice = Objects.requireNonNull(totalPrice, "totalPrice cannot be null");
    }
//    constructor for creating one
    public Order(
            Long userId,
            Long cartId,
            BigDecimal subTotal,
            BigDecimal shippingFee,
            BigDecimal discountAmount,
            BigDecimal totalPrice
    )
    {
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.cartId = Objects.requireNonNull(cartId, "cartId cannot be null");
        this.subTotal = Objects.requireNonNull(subTotal, "subTotal cannot be null");
        this.shippingFee = (shippingFee == null) ? BigDecimal.ZERO : shippingFee;
        this.discountAmount = (discountAmount == null) ? BigDecimal.ZERO : discountAmount;
        this.orderStatus = OrderStatus.PENDING_PAYMENT;
        this.createdAt = Instant.now();
        this.totalPrice = Objects.requireNonNull(totalPrice, "totalPrice cannot be null");
        if(subTotal.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("subTotal must be greater than zero");
        }
        if(this.shippingFee.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("shippingFee must not be negative");
        }
        if(this.discountAmount.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("discountAmount must not be negative");
        }
    }
//    getters
    public Long getOrderId(){return this.orderId;}
    public Long getCartId(){return this.cartId;}
    public OrderStatus getOrderStatus(){return this.orderStatus;}
    public Long getUserId(){return this.userId;}
    public BigDecimal getSubTotal(){return this.subTotal;}
    public BigDecimal getShippingFee(){return this.shippingFee;}
    public BigDecimal getDiscountAmount(){return this.discountAmount;}
    public BigDecimal getTotalPrice(){return this.totalPrice;}
    public Instant getCreatedAt(){return this.createdAt;}
}
