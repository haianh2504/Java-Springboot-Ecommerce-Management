package checkout.service;

import cart.entities.Cart;
import cart.service.CartManagementService;
import cart_item.entities.CartItem;
import cart_item.service.CartItemManagementService;
import checkout.entities.CheckoutItem;
import discount.service.DiscountService;
import common.exception.business.detailed_exceptions.CartIsEmptyException;
import common.exception.business.detailed_exceptions.CartOwnershipMismatchException;
import order.entities.Order;
import order.service.OrderManagementService;
import order_item.service.OrderItemManagementService;
import product.entities.Product;
import product.service.ProductManagementService;
import shipping.ShippingStrategy;
import transaction_management.TransactionManagement;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CheckoutServiceImpl implements CheckoutService {
    private final CartManagementService cartManagementService;
    private final CartItemManagementService cartItemManagementService;
    private final ShippingStrategy shippingStrategy;
    private final DiscountService discountService;
    private final OrderManagementService orderManagementService;
    private final OrderItemManagementService orderItemManagementService;
    private final ProductManagementService productManagementService;
    private final TransactionManagement transactionManagement;

    public CheckoutServiceImpl(
            CartManagementService cartManagementService,
            CartItemManagementService cartItemManagementService,
            ShippingStrategy shippingStrategy,
            DiscountService discountService,
            OrderManagementService orderManagementService,
            OrderItemManagementService orderItemManagementService,
            ProductManagementService productManagementService,
            TransactionManagement transactionManagement
    ) {
        this.cartManagementService = Objects.requireNonNull(
                cartManagementService, "cartManagementService cannot be null"
        );
        this.cartItemManagementService = Objects.requireNonNull(
                cartItemManagementService, "cartItemManagementService cannot be null"
        );
        this.shippingStrategy = Objects.requireNonNull(
                shippingStrategy, "shippingStrategy cannot be null"
        );
        this.discountService = Objects.requireNonNull(
                discountService, "discountService cannot be null"
        );
        this.orderManagementService = Objects.requireNonNull(
                orderManagementService, "orderManagementService cannot be null"
        );
        this.orderItemManagementService = Objects.requireNonNull(
                orderItemManagementService, "orderItemManagementService cannot be null"
        );
        this.productManagementService = Objects.requireNonNull(
                productManagementService, "productManagementService cannot be null"
        );
        this.transactionManagement = Objects.requireNonNull(
                transactionManagement, "transactionManagement cannot be null"
        );
    }

    @Override
    public Order checkout(Long userId, Long cartId) {
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(cartId, "cartId is required");

        return transactionManagement.execute(() -> {
            Cart cart = cartManagementService.getCartById(cartId);
            validateCartOwnership(cart, userId);

            // Reserve the cart before creating any order data. This UPDATE is not a
            // premature commit: it uses the same connection and is reverted together
            // with order/stock changes if any later checkout step fails.
            cartManagementService.checkoutCart(cartId);

            List<CartItem> cartItems = cartItemManagementService.getCartItemsByCartId(cartId);
            if (cartItems.isEmpty()) {
                throw new CartIsEmptyException();
            }

            List<CheckoutItem> checkoutItems = buildCheckoutItems(cartItems);

            BigDecimal subTotal = calculateSubtotal(checkoutItems);
            BigDecimal shippingFee = shippingStrategy.calculateShippingFee(checkoutItems);
            BigDecimal discountAmount = discountService.calculateDiscountAmount(subTotal);
            validatePriceAdjustments(subTotal, shippingFee, discountAmount);
            BigDecimal totalPrice = subTotal.add(shippingFee).subtract(discountAmount);

            Order order = orderManagementService.createOrder(
                    userId,
                    cartId,
                    subTotal,
                    shippingFee,
                    discountAmount,
                    totalPrice
            );

            for (CheckoutItem checkoutItem : checkoutItems) {
                CartItem cartItem = checkoutItem.cartItem();
                orderItemManagementService.createNewOrderItem(
                        order.getOrderId(),
                        cartItem.getProductId(),
                        cartItem.getNumber(),
                        checkoutItem.unitPrice()
                );
            }

            for (CheckoutItem checkoutItem : checkoutItems) {
                productManagementService.decreaseStockQuantity(
                        checkoutItem.product().getId(),
                        checkoutItem.cartItem().getNumber()
                );
            }
            return order;
        });
    }

    private void validateCartOwnership(Cart cart, Long userId) {
        if (!Objects.equals(cart.getUserId(), userId)) {
            throw new CartOwnershipMismatchException(userId, cart.getCartId());
        }
    }

    private List<CheckoutItem> buildCheckoutItems(List<CartItem> cartItems) {
        List<CheckoutItem> checkoutItems = new ArrayList<>(cartItems.size());
        for (CartItem cartItem : cartItems) {
            Product product = cartItemManagementService.validatedCartItemToOrderItem(cartItem);
            checkoutItems.add(new CheckoutItem(cartItem, product));
        }
        return List.copyOf(checkoutItems);
    }

    private BigDecimal calculateSubtotal(List<CheckoutItem> checkoutItems) {
        BigDecimal subTotal = BigDecimal.ZERO;
        for (CheckoutItem checkoutItem : checkoutItems) {
            subTotal = subTotal.add(checkoutItem.lineTotal());
        }
        return subTotal;
    }

    private void validatePriceAdjustments(
            BigDecimal subTotal,
            BigDecimal shippingFee,
            BigDecimal discountAmount
    ) {
        Objects.requireNonNull(shippingFee, "shippingFee is required");
        Objects.requireNonNull(discountAmount, "discountAmount is required");
        if (shippingFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("shippingFee cannot be negative");
        }
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("discountAmount cannot be negative");
        }
        if (discountAmount.compareTo(subTotal.add(shippingFee)) > 0) {
            throw new IllegalArgumentException("discountAmount cannot exceed the payable amount");
        }
    }
}
