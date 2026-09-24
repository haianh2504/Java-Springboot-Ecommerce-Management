package common.exception.global_exception_handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DATA includes: @ToString, @EqualsAndHashCode, @Getter, @Setter, @RequiredArgsConstructor
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class ErrorResponse{
    private int httpStatusCode;
    private String errorMessage;
    private LocalDateTime timestamp;
}
