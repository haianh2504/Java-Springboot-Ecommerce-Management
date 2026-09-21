package product.repository;

import product.entities.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public final class JdbcProductRepository implements ProductRepository {
    private final Connection connection;
//    constructor
    public JdbcProductRepository(Connection connection)
    {
        this.connection = Objects.requireNonNull(connection, "Connection cannot be null");

    }
//    save Product
    @Override
    public Product save(Product product)
    {
        String sql = """
                INSERT INTO products(
                name,
                quantity,
                price,
                status,
                created_at
                )
                VALUES(?,?,?,?,?)
                RETURNING id
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, product.getName().name());
            ps.setInt(2, product.getQuantity());
            ps.setBigDecimal(3,product.getBasePrice());
            ps.setString(4,product.getStatus().toString());
            ps.setTimestamp(5,java.sql.Timestamp.from(product.getCreatedAt()));
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()){
                    throw new SQLException("Saving and return product failed");
                }
                return new Product(
                        rs.getLong("id"),
                        product.getName(),
                        product.getQuantity(),
                        product.getBasePrice(),
                        product.getStatus(),
                        product.getCreatedAt()
                );
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error while saving product into DATABASE: " + e.getMessage(),e);
        }
    }
//    find product by id
    @Override
    public Optional<Product> findById(Long productId)
    {
        String sql = """
                SELECT
                name,
                quantity,
                price,
                status,
                created_at
                FROM products
                WHERE id = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            // gán id vào ?
            ps.setLong(1, productId);
            // chạy lệnh SELECT
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next())
                {
                    return Optional.empty();
                }
                ProductName name = new ProductName(rs.getString("name"));
                int quantity = rs.getInt("quantity");
                BigDecimal price = rs.getBigDecimal("price");
                ProductStatus status = ProductStatus.valueOf(rs.getString("status"));
                Instant created_at = rs.getTimestamp("created_at").toInstant();
                return Optional.of(new Product(
                        productId,
                        name,
                        quantity,
                        price,
                        status,
                        created_at
                ));
            }
        }catch(SQLException e)
        {
            throw new RuntimeException("Error while searching for products: " + e.getMessage(), e);
        }
    }
//    find product by name
    @Override
    public Optional<Product> findByName(ProductName name)
    {
        String sql = """
                SELECT
                id,
                name,
                quantity,
                price,
                status,
                created_at
                FROM products WHERE name = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, name.name());
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()){
                    return Optional.empty();
                }
                Long id = rs.getLong("id");
                int quantity = rs.getInt("quantity");
                BigDecimal price = rs.getBigDecimal("price");
                Instant created_at = rs.getTimestamp("created_at").toInstant();
                ProductStatus status = ProductStatus.valueOf(rs.getString("status"));
                return Optional.of(new Product(
                        id,
                        name,
                        quantity,
                        price,
                        status,
                        created_at
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while searching for product: " + e.getMessage(),e);
        }
    }
//    search products by optional price range and status
    @Override
    public List<Product> search(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            ProductStatus status
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, name, quantity, price, status, created_at
                FROM products
                WHERE 1 = 1
                """);
        List<Object> parameters = new ArrayList<>();

        if (minPrice != null) {
            sql.append(" AND price >= ?");
            parameters.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            parameters.add(maxPrice);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            parameters.add(status.name());
        }
        sql.append(" ORDER BY id");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int index = 0; index < parameters.size(); index++) {
                Object parameter = parameters.get(index);
                if (parameter instanceof BigDecimal price) {
                    ps.setBigDecimal(index + 1, price);
                } else {
                    ps.setString(index + 1, parameter.toString());
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
                return products;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while searching for products: " + e.getMessage(), e);
        }
    }
//    delete product by id
    @Override
    public void deleteById(Long productId) {
        String sql = "DELETE FROM products WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while deleting product: " + e.getMessage(), e);
        }
    }
//    update product after changes
    @Override
    public void update(Product product)
    {
        String sql = """
                UPDATE products
                SET
                name = ?,
                quantity = ?,
                price = ?,
                status = ?
                WHERE id = ?;
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, product.getName().name());
            ps.setInt(2,product.getQuantity());
            ps.setBigDecimal(3, product.getBasePrice());
            ps.setString(4, product.getStatus().toString());
            ps.setLong(5, product.getId());
            ps.executeUpdate();
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while updating product: " + e.getMessage(), e);
        }
    }
//    decrease quantity
    @Override
    public Optional<Product> decreaseQuantity(Long productId, int quantity) {
        String sql = """
                UPDATE products
                SET quantity = quantity - ?
                WHERE id = ?
                  AND status = 'ACTIVE'
                  AND quantity >= ?
                RETURNING id, name, quantity, price, status, created_at;
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, quantity);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            // The stock check and subtraction happen in one statement. PostgreSQL
            // re-checks the predicate after waiting for a concurrent row lock, which
            // prevents two buyers from both spending the same remaining inventory.
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapProduct(rs)) : Optional.empty();
            }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while decreasing product's quantity: " + e.getMessage(),e);
        }
    }
//    increase quantity
    @Override
    public Optional<Product> increaseQuantity(Long productId, int quantity) {
        String sql = """
                UPDATE products
                SET quantity = quantity + ?
                WHERE id = ?
                RETURNING id, name, quantity, price, status, created_at;
        """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, quantity);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapProduct(rs)) : Optional.empty();
            }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while increasing product's quantity: " + e.getMessage(),e);
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getLong("id"),
                new ProductName(rs.getString("name")),
                rs.getInt("quantity"),
                rs.getBigDecimal("price"),
                ProductStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
