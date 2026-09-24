package cart_item.repository;

import cart_item.entities.CartItem;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import javax.sql.DataSource;

@Repository
public class JdbcCartItemRepository implements CartItemRepository {
    private final DataSource dataSource;
//    constructor
    public JdbcCartItemRepository(DataSource dataSource) {
        // DataSource thay thế Connection singleton và hỗ trợ connection pool của Spring Boot.
        this.dataSource = new TransactionAwareDataSourceProxy(
                Objects.requireNonNull(dataSource, "DataSource cannot be null")
        );
    }
//    get all cart items by cart id
    @Override
    public List<CartItem> findByCartId(Long cartId) {
        String sql = """
                SELECT
                id,
                cart_id,
                product_id,
                quantity
                FROM cart_items 
                WHERE cart_id = ?
                """;
        List<CartItem> cartItems = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1, cartId);
            try(ResultSet rs = ps.executeQuery())
            {
                while(rs.next())
                {
                    cartItems.add(new CartItem(
                            rs.getLong("id"),
                            rs.getLong("cart_id"),
                            rs.getLong("product_id"),
                            rs.getInt("quantity")
                    ));
                }
            }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while finding cartItems by cartId: " + e.getMessage(), e);
        }
        return cartItems;
    }
//    get cart item by cart and product id
    @Override
    public Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId) {
        String sql = """
                SELECT
                id,
                cart_id,
                product_id,
                quantity
                FROM cart_items
                WHERE cart_id = ? AND product_id = ?;
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1, cartId);
            ps.setLong(2, productId);
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()){
                    return Optional.empty();
                }
                return Optional.of(
                        new CartItem(rs.getLong("id"),
                                rs.getLong("cart_id"),
                                rs.getLong("product_id"),
                                rs.getInt("quantity")
                        )
                );
            }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while finding cartItem by cartId and productId: " + e.getMessage(), e);
        }
    }
//    find one cart item by its id
    @Override
    public Optional<CartItem> findByCartItemId(Long cartItemId) {
        String sql = """
                SELECT 
                id,
                cart_id,
                product_id,
                quantity
                FROM cart_items WHERE id = ?;
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1,cartItemId);
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()){
                    return Optional.empty();
                }
                return Optional.of(
                        new CartItem(
                                rs.getLong("id"),
                                rs.getLong("cart_id"),
                                rs.getLong("product_id"),
                                rs.getInt("quantity")
                        )
                );
            }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while finding cartItem by cartItemId: " + e.getMessage(), e);
        }
    }
//    save cart item
    @Override
    public CartItem save(CartItem cartItem) {
        String sql = """
                INSERT INTO cart_items(cart_id, product_id, quantity)
                VALUES(?, ?, ?)
                RETURNING id
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1,cartItem.getCartId());
            ps.setLong(2, cartItem.getProductId());
            ps.setLong(3, cartItem.getNumber());
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()){
                    throw new RuntimeException("Error while saving and returning cartItem");
                }
                return new CartItem(
                        rs.getLong("id"),
                        cartItem.getCartId(),
                        cartItem.getProductId(),
                        cartItem.getNumber()
                );
            }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while saving cartItem: " + e.getMessage(), e);
        }
    }
//    delete all cartItems by cartId
    @Override
    public void deleteAllByCartId(Long cartId) {
        String sql = """
                DELETE FROM cart_items
                WHERE cart_id = ?;
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1, cartId);
            ps.executeUpdate();
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while deleting cartItems by cartId: " + e.getMessage(), e);
        }
    }
//    delete cart item by cartId and productId
    @Override
    public void deleteByCartIdAndProductId(Long cartId, Long productId) {
        String sql = """
                DELETE FROM cart_items
                WHERE cart_id = ? AND product_id = ?;
        """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1, cartId);
            ps.setLong(2, productId);
            ps.executeUpdate();
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while deleting cartItem by cartId and productId: " + e.getMessage(), e);
        }
    }

//    delete cart item by cart item id
    @Override
    public void deleteByCartItemId(Long cartItemId) {
        String sql = """
                DELETE FROM cart_items
                WHERE id = ?;
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1, cartItemId);
            ps.executeUpdate();
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while deleting cartItem by cartItemId: " + e.getMessage(), e);
        }
    }

    //    update cart item - usually quantity
    @Override
    public void update(CartItem cartItem) {
        String sql = """
                UPDATE cart_items
                SET quantity = ?
                WHERE id = ?;
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, cartItem.getNumber());
            ps.setLong(2, cartItem.getCartItemId());
            ps.executeUpdate();
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while updating cartItem by cartId and productId: " + e.getMessage(), e);
        }
    }
}
