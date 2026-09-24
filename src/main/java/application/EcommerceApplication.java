package application;

import cart.entities.Cart;
import cart_item.entities.CartItem;
import order.entities.Order;
import order_item.entities.OrderItem;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import product.entities.Product;
import user.entities.User;

// SpringBootApplication đã bao gồm: @Component / @EnableAutoConfiguration / @Configuration
// @EnableAutoConfiguration: Spring Boot tự động cấu hình ứng dụng dựa trên dependency hiện có.
// Đánh dấu là một class cung cấp cấu hình ( tương đương @Configuration )
// @ComponentScan:
//scanBasePackages yêu cầu Spring tìm các class như:
//@RestController
//@Service
//@Repository
//@Component
//@Configuration
//@RestControllerAdvice
@SpringBootApplication(
        // Quét và đăng ký Bean
        scanBasePackages = {
        "user",
        "product",
        "cart",
        "cart_item",
        "order",
        "order_item",
        "checkout",
        "discount",
        "shipping",
        "common",
        "transaction_management",
})

@EntityScan(
        basePackageClasses = {
                User.class,
                Product.class,
                Cart.class,
                CartItem.class,
                Order.class,
                OrderItem.class
        }
)
//@EnableJpaRepositories(
//        basePackageClasses = {
//                "user.repository",
//                "product.repository",
//                "cart.repository",
//                "cart_item.repository",
//                "order.repository",
//                "order_item.repository"
//        }
//)
public class EcommerceApplication {
    public static void main(String[] args) {
        // Tạo Spring Application Context.
        //•
        //Đọc cấu hình ứng dụng.
        //•
        //Tạo và quản lý các bean.
        //•
        //Thực hiện dependency injection.
        //•
        //Chạy auto-configuration.
        //•
        //Khởi động embedded web server, thường là Tomcat.
        //•
        //Đưa REST API vào trạng thái có thể nhận HTTP request
        SpringApplication.run(EcommerceApplication.class,args);
    }
}
