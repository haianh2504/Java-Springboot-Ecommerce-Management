package user.entities;
import common.exception.business.detailed_exceptions.AccountBannedException;
import common.exception.business.detailed_exceptions.UserAlreadyActive;
import common.exception.business.detailed_exceptions.UserAlreadyBanned;
import jakarta.persistence.*;
import lombok.Builder;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import user.entities.email.Email;
import user.entities.email.EmailConverter;
import user.entities.password_hash.PasswordHash;
import user.entities.password_hash.PasswordHashConverter;
import user.entities.person_name.PersonName;
import user.entities.person_name.PersonNameConverter;
import user.entities.phone_number.PhoneNumber;
import user.entities.phone_number.PhoneNumberConverter;

import java.time.Instant;
import java.util.Objects;
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_users_email",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "uq_users_phone_number",
                        columnNames = "phone_number"
                )
        }
)
@Check(constraints = "btrim(name) <> ''")
@Check(constraints = "btrim(email) <> ''")
@Check(constraints = "btrim(password_hash) <> ''")
@Check(constraints = "phone_number IS NULL OR btrim(phone_number) <> ''")
@Access(AccessType.FIELD)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = PasswordHashConverter.class)
    @Column(name = "password_hash", nullable = false, length = 255)
    private PasswordHash passwordHash;

    @Convert(converter = PersonNameConverter.class)
    @Column(name = "name",nullable = false,length = 255)
    private PersonName name;

    @Convert(converter = PhoneNumberConverter.class)
    @Column(name = "phone_number",nullable = true, length = 50)
    private PhoneNumber phoneNumber; // can add later

    @Convert(converter = EmailConverter.class)
    @Column(name = "email", nullable = false, length = 320)
    private Email email; // compulsory

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false, columnDefinition = "user_role_enum")
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "user_status_enum")
    private UserStatus status;

    @Column(name = "created_at",nullable = false, updatable = false)
    private Instant createdAt;

    private static void validateBasicInfo(
            PasswordHash passwordHash,
            PersonName name,
            Email email,
            UserRole userRole
    ) {
        Objects.requireNonNull(passwordHash, "User passwordHash cannot be null");
        Objects.requireNonNull(name, "User name cannot be null");
        Objects.requireNonNull(email,"User email cannot be null");
        Objects.requireNonNull(userRole, "User role cannot be null");
    }
//    constructor - full info
    @Builder
    public User(PasswordHash passwordHash,PersonName name,PhoneNumber phoneNumber, Email email, UserRole userRole)
    {
        validateBasicInfo(passwordHash,name,email,userRole);
        // id có thể null -> postgreSQL tự generate
        this.name = name;
        this.passwordHash = passwordHash;
        if(phoneNumber == null)
        {
            this.status = UserStatus.PENDING;
        }
        else{
            this.phoneNumber = phoneNumber;
            this.status = UserStatus.ACTIVE;
        }
        this.email = email;
        this.userRole = userRole;
        this.createdAt = Instant.now();
    }

//    constructor - SQL return
    public User(Long id,PasswordHash passwordHash,PersonName name,PhoneNumber phoneNumber, Email email, UserRole userRole, UserStatus userStatus,Instant createdAt)
    {
        validateBasicInfo(passwordHash,name,email,userRole);
        this.id = Objects.requireNonNull(id, "User id cannot be null");
        this.name = name;
        this.passwordHash = passwordHash;
        // phoneNumber can be null
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.userRole = userRole;
        this.status = Objects.requireNonNull(userStatus,"User status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt,"Timestamp createdAt cannot be null");
    }
//    No arguments constructor
    protected User() {}
//    getters
    public Long getId()
    {
        return this.id;
    }
    public PasswordHash getPasswordHash()
    {
        return this.passwordHash;
    }
    public PersonName getName()
    {
        return this.name;
    }
    public PhoneNumber getPhoneNumber()
    {
        return this.phoneNumber;
    }
    public Email getEmail()
    {
        return this.email;
    }
    public UserRole getRole()
    {
        return this.userRole;
    }
    public UserStatus getStatus()
    {
        return this.status;
    }
    public Instant getTimeCreated()
    {
        return this.createdAt;
    }
//    add phoneNumber
    private void addPhoneNumber(PhoneNumber phoneNumber)
    {
        if(this.phoneNumber != null) return;
        this.phoneNumber = Objects.requireNonNull(phoneNumber,"PhoneNumber cannot be null");
    }
//    activate account
    public void activate(PhoneNumber phoneNumber)
    {
        // if user has already been activated
        if(this.status == UserStatus.ACTIVE){
            throw new UserAlreadyActive(this);
        }
        // if input null
        if(phoneNumber == null){
            throw new NullPointerException("PhoneNumber cannot be null");
        }
        addPhoneNumber(phoneNumber);
        this.status = UserStatus.ACTIVE;
    }
//    banned account
    public void banned()
    {
        if(this.status == UserStatus.BANNED)
        {
            throw new UserAlreadyBanned(this);
        }
        this.status = UserStatus.BANNED;
    }
//    change role
    private void changeRole(UserRole newRole)
    {
        if(newRole == null)
        {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.userRole = newRole;
    }
    public void authorize()
    {
        Objects.requireNonNull(userRole, "User role cannot be null");
        if(this.status == UserStatus.BANNED)
        {
            throw new AccountBannedException();
        }
        this.changeRole(UserRole.ADMIN);
    }
//    change name
    public void changeName(PersonName newName)
    {
        if(newName == null)
        {
            throw new NullPointerException("New username cannot be null");
        }
        this.name = newName;
    }
//    change phone number
    public void changePhoneNumber(PhoneNumber phoneNumber)
    {
        if(phoneNumber == null)
        {
            throw new NullPointerException("New phoneNumber cannot be null");
        }
        if(phoneNumber.phoneNumber().equals(this.phoneNumber.phoneNumber())) return;
        this.phoneNumber = phoneNumber;
    }
//    change email
    public void changeEmail(Email email)
    {
        if(email == null)
        {
            throw new NullPointerException("New email cannot be null");
        }
        this.email = email;
    }
}
