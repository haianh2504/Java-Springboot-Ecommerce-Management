package user.service;

import user.entities.*;

public interface UserManagementService {
//    find user by ID
    public User findUserById(Long userId);
//    find user by email
    public User findUserByEmail(Email email);
//    create new User
    public User createUser(RawPassword rawPassword, PersonName name, PhoneNumber phoneNumber, Email email, UserRole userRole);
//    make status ACTIVE if have full info
    public User activateUser(Long userId,PhoneNumber phoneNumber);
//    make status BANNED
//    change Name
    public User changeUserName(Long userId, PersonName newName);
//    change phoneNumber
    public User changePhoneNumber(Long userId, PhoneNumber newPhoneNumber);
//    change email
    public User changeEmail(Long userId, Email newEmail);
//    Admin authorize
    public User promoteToAdmin(Long adminId,Long userId);
}
