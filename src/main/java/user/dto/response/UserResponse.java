package user.dto.response;

import user.entities.User;
import user.entities.UserRole;
import user.entities.UserStatus;

import java.time.Instant;
import java.util.Objects;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phoneNumber,
        UserRole role,
        UserStatus status,
        Instant createdAt
) {
    public static UserResponse from(User user)
    {
        Objects.requireNonNull(user, "User cannot be null");
        return new UserResponse(
                user.getId(),
                user.getName().name(),
                user.getEmail().email(),
                user.getPhoneNumber() == null ? null : user.getPhoneNumber().phoneNumber(),
                user.getRole(),
                user.getStatus(),
                user.getTimeCreated()

        );
    }
}
