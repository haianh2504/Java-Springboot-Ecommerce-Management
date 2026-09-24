package cart_item.entities;

import jakarta.persistence.*;
import jakarta.validation.Constraint;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;

import java.util.Objects;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_cart_items_cart_product",
                        columnNames = {"cart_id", "product_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_cart_items_product_id",
                        columnList = "product_id"
                )
        }
)
@Check(
        name = "ck_cart_items_quantity_positive",
        constraints = "quantity > 0"
)
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long cartItemId;

    @Column(
            name = "cart_id",
            nullable = false
    )
    private Long cartId;

    @Column(
            name = "product_id",
            nullable = false,
            updatable = false
    )
    private Long productId;

    @Column(name = "quantity", nullable = false)
    @ColumnDefault("1")
    private int number;

//    no argument constructor for JPA
    protected CartItem() {}
//    constructor for SQL return
    public CartItem(Long cartItemId, Long cartId, Long productId, int number)
    {
        if(cartItemId == null)
        {
            throw new NullPointerException("CartItem ID cannot be null");
        }
        this.cartId = Objects.requireNonNull(cartId, "Cart ID cannot be null");
        if(productId == null)
        {
            throw new NullPointerException("Product ID cannot be null");
        }
        // need to check if the product Id exists
        if(number <= 0) // also need to check the upper bound
        {
            throw new IllegalArgumentException("Cart item number must be greater than 0");
        }
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.number = number;
    }
//    constructor for creating one
    public CartItem(Long cartId, Long productId, int number)
    {
        this.cartId = Objects.requireNonNull(cartId, "Cart ID cannot be null");
        this.productId = Objects.requireNonNull(productId, "Product ID cannot be null");
        if(number <= 0){
            throw new IllegalArgumentException("Cart item number must be greater than 0");
        }
        this.number = number;
    }
//    getters
    public Long getCartItemId(){return this.cartItemId;}
    public Long getCartId(){return this.cartId;}
    public Long getProductId()
    {
        return this.productId;
    }
    public int getNumber()
    {
        return this.number;
    }
//    setter
    public void changeNumber(int number)
    {
        if(number <= 0) // also need to check the upper bound
        {
            throw new IllegalArgumentException("Cart item number must be greater than 0");
        }
        else if(number == this.number)
        {
            return;
        }
        this.number = number;
    }
}
