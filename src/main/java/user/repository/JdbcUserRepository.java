package user.repository;

import user.entities.*;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.stereotype.Repository;
import user.entities.email.Email;
import user.entities.password_hash.PasswordHash;
import user.entities.person_name.PersonName;
import user.entities.phone_number.PhoneNumber;

import java.sql.*;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;

@Repository
public class JdbcUserRepository implements UserRepository{
    private final DataSource dataSource;
//    constructor
    public JdbcUserRepository(DataSource dataSource)
    {
        // Proxy bảo đảm các repository dùng chung connection khi đang ở trong cùng một transaction.
        this.dataSource = new TransactionAwareDataSourceProxy(
                Objects.requireNonNull(dataSource, "DataSource cannot be null")
        );
    }
//    save user
    @Override
    public User save(User user)
    {
        // SQL
        String sql = """
                INSERT INTO users(
                name,
                phone_number,
                email,
                role,
                status,
                password_hash,
                created_at
                )
                VALUES(?,?,?,?,?,?,?)
                RETURNING id
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, user.getName().name());
            if(user.getPhoneNumber() != null){
                ps.setString(2,user.getPhoneNumber().toString());
            }
            else{
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3,user.getEmail().toString());
            ps.setString(4,user.getRole().name());
            ps.setString(5,user.getStatus().name());
            ps.setString(6, user.getPasswordHash().passwordHash());
            // from: Instant | valueOf: LocalDateTime
            ps.setTimestamp(7,java.sql.Timestamp.from(user.getTimeCreated()));
            // Thực thi câu lệnh INSERT xuống DB
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return new User(
                            rs.getLong("id"),
                            user.getPasswordHash(),
                            user.getName(),
                            user.getPhoneNumber(),
                            user.getEmail(),
                            user.getRole(),
                            user.getStatus(),
                            user.getTimeCreated()
                    );
                }
                else{
                    throw new SQLException("Saving user failed, no ID obtained");
                }
            }catch(SQLException e){
                throw new SQLException("Saving and Returning user failed", e);
            }
        }catch(SQLException e)
        {
            throw new RuntimeException("Error while saving user into DATABASE: " + e.getMessage(),e);
        }
    }
//    update user by id
    @Override
    public User update(User user)
    {
        String sql = """
                UPDATE users
                SET
                name = ?,
                phone_number = ?,
                email = ?,
                role = ?,
                status = ?,
                password_hash = ?
                WHERE id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, user.getName().name());
            if(user.getPhoneNumber() == null)
            {
                ps.setNull(2, Types.VARCHAR);
            }
            else ps.setString(2,user.getPhoneNumber().toString());
            ps.setString(3,user.getEmail().toString());
            ps.setString(4,user.getRole().name());
            ps.setString(5, user.getStatus().name());
            ps.setString(6, user.getPasswordHash().passwordHash());
            ps.setLong(7, user.getId());
            int updatedRows = ps.executeUpdate();
            if(updatedRows == 0)
            {
                throw new RuntimeException("Updating user failed, no user found with id " + user.getId());
            }
            return user;
        } catch (SQLException e){
            throw new RuntimeException("Error while updating user's information: " + e.getMessage(),e);
        }
    }
//    find user by id
    @Override
    public Optional<User> findById(Long id){
        String sql = """
                SELECT
                name,
                phone_number,
                email,
                role,
                status,
                password_hash,
                created_at
                FROM users WHERE id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1, id);
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()){
                    return Optional.empty();
                }
                PersonName name = new PersonName(rs.getString("name"));
                Email email = new Email(rs.getString("email"));
                UserRole role = UserRole.valueOf(rs.getString("role"));
                UserStatus status = UserStatus.valueOf(rs.getString("status"));
                PasswordHash password_hash = new PasswordHash(rs.getString("password_hash"));
                Instant created_at = rs.getTimestamp("created_at").toInstant();
                String sqlPhone = rs.getString("phone_number");
                if(sqlPhone != null){
                    PhoneNumber phone_number = new PhoneNumber(sqlPhone);
                    User sqlUser = new User(
                            id,
                            password_hash,
                            name,
                            phone_number,
                            email,
                            role,
                            status,
                            created_at
                    );
                    return Optional.of(sqlUser);
                }
                else{
                    User sqlUser = new User(
                            id,
                            password_hash,
                            name,
                            null,
                            email,
                            role,
                            status,
                            created_at
                    );
                    return Optional.of(sqlUser);
                }
            }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while searching user: " + e.getMessage(),e);
        }
    }
//    find user by email
    @Override
    public Optional<User> findByEmail(Email email){
        String sql = """
                SELECT
                id,
                name,
                phone_number,
                email,
                role,
                status,
                password_hash,
                created_at
                FROM users
                WHERE email = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            // gán email vào ô ?
            ps.setString(1, email.toString());
            // chạy lệnh SELECT
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()) return Optional.empty();
                // lay thuoc tinh
                Long id = rs.getLong("id");
                PersonName name = new PersonName(rs.getString("name"));
                Email user_email = new Email(rs.getString("email"));
                UserRole role = UserRole.valueOf(rs.getString("role"));
                UserStatus status = UserStatus.valueOf(rs.getString("status"));
                PasswordHash password_hash = new PasswordHash(rs.getString("password_hash"));
                Instant created_at = rs.getTimestamp("created_at").toInstant();
                String sqlPhone = rs.getString("phone_number");
                if(sqlPhone != null)
                {
                    PhoneNumber phone_number = new PhoneNumber(sqlPhone);
                    return Optional.of(new User(
                            id,
                            password_hash,
                            name,
                            phone_number,
                            user_email,
                            role,
                            status,
                            created_at
                    ));
                }
                else {
                    return Optional.of(new User(
                            id,
                            password_hash,
                            name,
                            null,
                            user_email,
                            role,
                            status,
                            created_at
                    ));
                }
            }
        }catch(SQLException e)
        {
            throw new RuntimeException("Error while searching user: " + e.getMessage(),e);
        }
    }
//    Find by phone number
    @Override
    public Optional<User> findByPhoneNumber(PhoneNumber phoneNumber){
        String sql = """
                SELECT * FROM users WHERE phone_number = ?;
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1,phoneNumber.toString());
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()) return Optional.empty();
                String sqlPhone = rs.getString("phone_number");
                PhoneNumber phone_number = (sqlPhone == null) ? null : new PhoneNumber(sqlPhone);
                return Optional.of(
                        new User(
                                rs.getLong("id"),
                                new PasswordHash(rs.getString("password_hash")),
                                new PersonName(rs.getString("name")),
                                phone_number,
                                new Email(rs.getString("email")),
                                UserRole.valueOf(rs.getString("role")),
                                UserStatus.valueOf(rs.getString("status")),
                                rs.getTimestamp("created_at").toInstant()
                        )
                );
            }
        }catch(SQLException e)
        {
            throw new RuntimeException("Error while searching user by phone number: " + e.getMessage(),e);
        }
    }
}
