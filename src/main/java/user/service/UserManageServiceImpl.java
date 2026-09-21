package user.service;

import exception.business.detailed_exceptions.AccountBannedException;
import exception.business.detailed_exceptions.EmailAlreadyInUseException;
import exception.business.detailed_exceptions.PhoneAlreadyInUseException;
import exception.business.detailed_exceptions.UserNotAuthorizedException;
import exception.resource.detailed_exceptions.UserNotFoundException;
import user.entities.email.Email;
import user.entities.password_hash.PasswordHash;
import user.entities.person_name.PersonName;
import user.entities.phone_number.PhoneNumber;
import user.repository.UserRepository;
import user.entities.*;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public final class UserManageServiceImpl implements UserManagementService{
    // Lấy UserRepository làm biến tham chiếu quyết định các phương thức
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
//    constructor
    public UserManageServiceImpl(UserRepository userRepository, PasswordHasher passwordHasher)
    {
        this.userRepository = Objects.requireNonNull(userRepository, "User repository cannot be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "Password hasher cannot be null");
    }
//    find user by ID
    @Override
    public User findUserById(Long userId) {
        Objects.requireNonNull(userId,"UserId cannot be null");
        return userRepository.findById(userId).orElseThrow(
                ()-> new UserNotFoundException(userId)
        );
    }
//    find user by Email
    @Override
    public User findUserByEmail(Email email) {
        Objects.requireNonNull(email, "User email cannot be null");
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException(email)
        );
    }
    //    create new user after REGISTER
    @Override
    public User createUser(RawPassword rawPassword, PersonName name, PhoneNumber phoneNumber, Email email, UserRole userRole){
        // check null
        Objects.requireNonNull(rawPassword, "Raw password cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(userRole, "User role cannot be null");
        // new User
        if(userRepository.findByEmail(email).isPresent())
        {
            throw new EmailAlreadyInUseException();
        }
        PasswordHash passwordHash = passwordHasher.hash(rawPassword);
        // phoneNumber is optional
        return userRepository.save(new User(passwordHash,name,phoneNumber,email,userRole));
    }
//    activate User
    @Override
    public User activateUser(Long userId, PhoneNumber phoneNumber)
    {
        Objects.requireNonNull(userId, "UserId cannot be null");
        Objects.requireNonNull(phoneNumber, "Phone number cannot be null");
        User user = userRepository.findById(userId)
               .orElseThrow(() -> new UserNotFoundException(userId));
        user.activate(phoneNumber);
        return userRepository.update(user);
    }
//    change name
    @Override
    public User changeUserName(Long userId, PersonName newName)
    {
        Objects.requireNonNull(userId, "UserId cannot be null");
        Objects.requireNonNull(newName, "New username cannot be null");
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(userId)
        );
        user.changeName(newName);
        return userRepository.update(user);
    }
//    change phone number
    @Override
    public User changePhoneNumber(Long userId, PhoneNumber newPhoneNumber){
        Objects.requireNonNull(userId, "UserId cannot be null");
        Objects.requireNonNull(newPhoneNumber, "Phone number cannot be null");
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(userId)
        );
        if(userRepository.findByPhoneNumber(newPhoneNumber).isPresent())
        {
            throw new PhoneAlreadyInUseException();
        }
        user.changePhoneNumber(newPhoneNumber);
        return userRepository.update(user);
    }
//    change email
    @Override
    public User changeEmail(Long userId, Email newEmail){
        Objects.requireNonNull(userId, "UserId cannot be null");
        Objects.requireNonNull(newEmail, "Email cannot be null");
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(userId)
        );
        if(userRepository.findByEmail(newEmail).isPresent())
        {
            throw new EmailAlreadyInUseException();
        }
        user.changeEmail(newEmail);
        return userRepository.update(user);
    }
//    change Role
    @Override
    public User promoteToAdmin(Long adminId,Long userId)
    {
        Objects.requireNonNull(adminId, "Admin Id cannot be null");
        Objects.requireNonNull(userId, "UserId cannot be null");
        User admin = userRepository.findById(adminId).orElseThrow(
                () -> new UserNotFoundException(adminId)
        );
        if(admin.getRole() != UserRole.ADMIN)
        {
            throw new UserNotAuthorizedException();
        }
        User targetUser = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(userId)
        );
        if(targetUser.getStatus() == UserStatus.BANNED)
        {
            throw new AccountBannedException();
        }
        targetUser.authorize();
        return userRepository.update(targetUser);
    }
}
