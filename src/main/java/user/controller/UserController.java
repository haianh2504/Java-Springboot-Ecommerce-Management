package user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import user.dto.request.*;
import user.dto.response.UserResponse;
import user.entities.*;
import user.service.UserManagementService;

@RestController
@RequestMapping("/api/v1/users")
// v1 is for starting, building root
// v2 is for updating...
// v3 is about break old code to a new one -> impact clients while doing
public class UserController {
    private final UserManagementService userManagementService;

    // Do not need @AutoWired because only one parameter
    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    // Create User: POST/api/v1/users
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    )
    {
        User createdUser = userManagementService.createUser(
                new RawPassword(request.getPassword()),
                new PersonName(request.getUsername()),
                new PhoneNumber(request.getPhoneNumber()),
                new Email(request.getEmail()),
                request.getUserRole()
        );
        UserResponse response = UserResponse.from(createdUser);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // GET user by Email
    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> getUserByEmail(
            @RequestParam(name = "email",required=true) @jakarta.validation.constraints.Email String email
    ){
        User user = userManagementService.findUserByEmail(
                new Email(email)
        );
        return ResponseEntity.ok(UserResponse.from(user));
    }

    // GET user by id
    @GetMapping("/{id}/by-id")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable("id") @Positive Long id
    )
    {
        User user = userManagementService.findUserById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(UserResponse.from(user));
    }

    // ACTIVATE user
    @PatchMapping("/{id}/activation")
    public ResponseEntity<UserResponse> activateUser(
            @Valid @RequestBody ActivateUserRequest request,
            @PathVariable("id") @Positive Long id
    )
    {
        User user = userManagementService.activateUser(
                id,
                new PhoneNumber(request.getPhoneNumber())
        );
        return ResponseEntity.ok(UserResponse.from(user));
    }

    // CHANGE phone number user
    @PatchMapping("/{id}/phone-number")
    public ResponseEntity<UserResponse> changePhoneNumber(
            @Valid @RequestBody ChangePhoneNumberRequest request,
            @PathVariable("id") @Positive Long id
    )
    {
        User user = userManagementService.changePhoneNumber(
                id,
                new PhoneNumber(request.getPhoneNumber())
        );
        return ResponseEntity.ok(UserResponse.from(user));
    }

    // CHANGE email
    @PatchMapping("/{id}/email")
    public ResponseEntity<UserResponse> changeEmail(
            @Valid @RequestBody ChangeEmailRequest request,
            @PathVariable("id") @Positive Long id
    )
    {
        User user = userManagementService.changeEmail(
                id,
                new Email(request.getNewEmail())
        );
        return ResponseEntity.ok(UserResponse.from(user));
    }

    // CHANGE user name
    @PatchMapping("/{id}/name")
    public ResponseEntity<UserResponse> changeUserName(
            @PathVariable("id") @Positive Long id,
            @Valid @RequestBody ChangeUserNameRequest request
    ) {
        User user = userManagementService.changeUserName(
                id,
                new PersonName(request.getNewUserName())
        );
        return ResponseEntity.ok(UserResponse.from(user));
    }

    // PROMOTE user to admin
    @PatchMapping("/{id}/role/admin")
    public ResponseEntity<UserResponse> promoteToAdmin(
            @PathVariable("id") @Positive Long id,
            @Valid @RequestBody PromoteToAdminRequest request
    ) {
        User user = userManagementService.promoteToAdmin(request.adminId(), id);
        return ResponseEntity.ok(UserResponse.from(user));
    }

}
