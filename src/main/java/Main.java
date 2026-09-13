import cart.entities.Cart;
import cart.repository.CartRepository;
import cart.repository.JdbcCartRepository;
import cart.service.CartManagementService;
import cart.service.CartManagementServiceImpl;
import cart_item.entities.CartItem;
import cart_item.repository.CartItemRepository;
import cart_item.repository.JdbcCartItemRepository;
import cart_item.service.CartItemManagementService;
import cart_item.service.CartItemManagementServiceImpl;
import checkout.service.CheckoutServiceImpl;
import checkout.service.CheckoutService;
import common.DatabaseConnection;
import discount.service.DiscountService;
import discount.service.DiscountServiceImpl;
import order.repository.JdbcOrderRepository;
import order.repository.OrderRepository;
import order.service.OrderManagementService;
import order.service.OrderManagementServiceImpl;
import order_item.repository.JdbcOrderItemRepository;
import order_item.repository.OrderItemRepository;
import order_item.service.OrderItemManagementService;
import order_item.service.OrderItemManagementServiceImpl;
import product.entities.Product;
import product.entities.ProductName;
import product.repository.JdbcProductRepository;
import product.repository.ProductRepository;
import product.service.ProductManagementService;
import product.service.ProductManagementServiceImpl;
import shipping.ShippingStrategy;
import shipping.WeightBasedStrategy;
import transaction_management.TransactionManagement;
import user.entities.*;
import user.repository.JdbcUserRepository;
import user.repository.UserRepository;
import user.service.UserManageServiceImpl;
import user.service.UserManagementService;
import java.math.BigDecimal;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        // mô tả: user -> tạo cart -> tạo order
        // tạo database connection trước
        Connection connection = DatabaseConnection.getConnection();
        // Repository setting up
        UserRepository userRepository = new JdbcUserRepository(connection);
        CartRepository cartRepository = new JdbcCartRepository(connection);
        CartItemRepository cartItemRepository = new JdbcCartItemRepository(connection);
        ProductRepository productRepository = new JdbcProductRepository(connection);
        OrderRepository orderRepository = new JdbcOrderRepository(connection);
        OrderItemRepository orderItemRepository = new JdbcOrderItemRepository(connection);
        // cart service setting up
        CartManagementService cartManagementService = new CartManagementServiceImpl(cartRepository,cartItemRepository);
        // cart Item service setting up
        CartItemManagementService cartItemManagementService = new CartItemManagementServiceImpl(cartItemRepository, productRepository);
        // user service setting up
        UserManagementService userManageService = new UserManageServiceImpl(userRepository);
        // product service setting up
        ProductManagementService productManagementService = new ProductManagementServiceImpl(productRepository);
        // checkout dependencies
        ShippingStrategy shippingStrategy = new WeightBasedStrategy(BigDecimal.valueOf(20_000));
        DiscountService discountService = new DiscountServiceImpl();
        OrderManagementService orderManagementService = new OrderManagementServiceImpl(orderRepository, shippingStrategy);
        OrderItemManagementService orderItemManagementService = new OrderItemManagementServiceImpl(
                orderItemRepository,
                orderRepository
        );
        TransactionManagement transactionManagement = new TransactionManagement(connection);
        // checkout service setting up
        CheckoutService checkoutService = new CheckoutServiceImpl(
                cartManagementService,
                cartItemManagementService,
                shippingStrategy,
                discountService,
                orderManagementService,
                orderItemManagementService,
                productManagementService,
                transactionManagement
        );
        // create product
        Product product1 = productManagementService.createNewPhysicalProduct(
                new ProductName("Iphone 16 pro max"),
                20,
                BigDecimal.valueOf(20_000_000),
                BigDecimal.valueOf(0.200)
        );
        Product product2 = productManagementService.createNewPhysicalProduct(
                new ProductName("Macbook pro m1"),
                50,
                BigDecimal.valueOf(30_000_000),
                BigDecimal.valueOf(1.200)
        );
        // create user
        User user = userManageService.createUser(
                new PasswordHash("abcde123"),
                new PersonName("Phan Hai Anh"),
                new PhoneNumber("0346586211"),
                new Email("haianh2504077@gmail.com"),
                UserRole.ADMIN
        );
        // create cart for the user
        Cart cart = cartManagementService.createCart(user.getId());
        // create cart item
        CartItem cartItem1 = cartItemManagementService.addNewCartItem(
                cart.getCartId(),
                product1.getId(),
                5
        );
        CartItem cartItem2 = cartItemManagementService.addNewCartItem(
                cart.getCartId(),
                product2.getId(),
                10
        );
        // khi bấm checkout
        checkoutService.checkout(user.getId(),cart.getCartId());

    }
}
