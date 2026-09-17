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
import transaction_management.TransactionManagement;
import user.entities.*;
import user.repository.JdbcUserRepository;
import user.repository.UserRepository;
import user.service.UserManageServiceImpl;
import user.service.UserManagementService;
import java.math.BigDecimal;
import java.sql.Connection;

public class Main {
    public static String toLowerCase(String s) {
        return s.toLowerCase();
    }
    public static void main(String[] args) {
        String input = "ACBAasdadsC";
        System.out.println(toLowerCase(input));
    }
}
