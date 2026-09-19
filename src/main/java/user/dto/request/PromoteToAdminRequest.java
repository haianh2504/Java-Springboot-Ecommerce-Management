package user.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PromoteToAdminRequest(
        @NotNull(message = "Admin ID cannot be null")
        @Positive(message = "Admin ID must be greater than zero")
        Long adminId
) {}
