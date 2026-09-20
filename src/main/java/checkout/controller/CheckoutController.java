package checkout.controller;

import cart.service.CartManagementService;
import cart_item.service.CartItemManagementService;
import discount.service.DiscountService;
import order.service.OrderManagementService;
import order_item.service.OrderItemManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import product.service.ProductManagementService;
import shipping.ShippingStrategy;
import transaction_management.TransactionManagement;

@RestController
@RequestMapping("/api/v1/checkouts")
public class CheckoutController {
    private final CartManagementService cartManagementService;
    private final CartItemManagementService cartItemManagementService;
    private final ShippingStrategy shippingStrategy;
    private final DiscountService discountService;
    private final OrderManagementService orderManagementService;
    private final OrderItemManagementService orderItemManagementService;
    private final ProductManagementService productManagementService;
    private final TransactionManagement transactionManagement;

    @Autowired
    public CheckoutController(
            CartManagementService cartManagementService,
            CartItemManagementService cartItemManagementService,
            ShippingStrategy shippingStrategy,
            DiscountService discountService,
            OrderManagementService orderManagementService,
            OrderItemManagementService orderItemManagementService,
            ProductManagementService productManagementService,
            TransactionManagement transactionManagement
    ) {
        this.cartManagementService = cartManagementService;
        this.cartItemManagementService = cartItemManagementService;
        this.shippingStrategy = shippingStrategy;
        this.discountService = discountService;
        this.orderManagementService = orderManagementService;
        this.orderItemManagementService = orderItemManagementService;
        this.productManagementService = productManagementService;
        this.transactionManagement = transactionManagement;
    }


}
