package cart.entities;

import common.exception.business.detailed_exceptions.CartAlreadyCheckedOutException;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "carts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_carts_user_id_and_id",
                        columnNames = {"user_id", "id"}
                )
        }
)
@Access(AccessType.FIELD)
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long cartId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "cart_status_enum")
    private CartStatus cartStatus;

//    constructor for SQL return
    public Cart(Long cartId, Long userId, Instant createdAt, CartStatus cartStatus) {
        this.cartId = Objects.requireNonNull(cartId, "cartId cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Timestamp createdAt cannot be null");
        this.cartStatus = Objects.requireNonNull(cartStatus, "cartStatus cannot be null");
    }

//    constructor for creating new one
    public Cart(Long userId) {
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.createdAt = Instant.now();
        this.cartStatus = CartStatus.ACTIVE;
    }

//    no args constructor for JPA setting up
    protected Cart() {
    }

//    getters
    public Long getCartId() {
        return this.cartId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public CartStatus getCartStatus() {
        return this.cartStatus;
    }

//    setter
    public void setCheckedOutStatus() {
        Objects.requireNonNull(cartStatus, "cartStatus cannot be null");
        if (this.cartStatus == CartStatus.CHECKED_OUT) {
            throw new CartAlreadyCheckedOutException();
        }
        this.cartStatus = CartStatus.CHECKED_OUT;
    }
}
