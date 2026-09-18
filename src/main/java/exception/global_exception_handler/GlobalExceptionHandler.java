package exception.global_exception_handler;

import exception.business.BusinessException;
import exception.business.detailed_exceptions.AccountBannedException;
import exception.business.detailed_exceptions.CartAlreadyCheckedOutException;
import exception.business.detailed_exceptions.CartIsEmptyException;
import exception.business.detailed_exceptions.CartItemAlreadyExistsException;
import exception.business.detailed_exceptions.CartOwnershipMismatchException;
import exception.business.detailed_exceptions.EmailAlreadyInUseException;
import exception.business.detailed_exceptions.IncompleteProfileException;
import exception.business.detailed_exceptions.InsufficientStockException;
import exception.business.detailed_exceptions.OrderItemAlreadyExistsException;
import exception.business.detailed_exceptions.PhoneAlreadyInUseException;
import exception.business.detailed_exceptions.ProductInactiveException;
import exception.business.detailed_exceptions.ProductNameAlreadyInUseException;
import exception.business.detailed_exceptions.UserAlreadyActive;
import exception.business.detailed_exceptions.UserAlreadyBanned;
import exception.business.detailed_exceptions.UserNotAuthorizedException;
import exception.resource.ResourceException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public final class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // RESOURCE EXCEPTION
    // Handles every requested resource that cannot be found.
    @ExceptionHandler(ResourceException.class)
    public <T extends ResourceException> ResponseEntity<ErrorResponse> handleResourceException(T exception) {
        HttpStatus status = HttpStatus.NOT_FOUND; // 404 NOT FOUND
        ErrorResponse response = ErrorResponse.builder()
                .httpStatusCode(status.value())
                .errorMessage(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    // FORBIDDEN BUSINESS EXCEPTION
    // Handles operations rejected because the user lacks permission or resource ownership.
    @ExceptionHandler({
            UserNotAuthorizedException.class,
            AccountBannedException.class,
            CartOwnershipMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleForbiddenBusinessException(BusinessException exception) {
        HttpStatus status = HttpStatus.FORBIDDEN; // 404 FORBIDDEN
        ErrorResponse response = ErrorResponse.builder()
                .httpStatusCode(status.value())
                .errorMessage(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    // CONFLICT BUSINESS EXCEPTION
    // Handles duplicate data and operations that conflict with the current resource state.
    @ExceptionHandler({
            EmailAlreadyInUseException.class,
            PhoneAlreadyInUseException.class,
            ProductNameAlreadyInUseException.class,
            UserAlreadyActive.class,
            UserAlreadyBanned.class,
            CartAlreadyCheckedOutException.class,
            CartItemAlreadyExistsException.class,
            OrderItemAlreadyExistsException.class,
            InsufficientStockException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictBusinessException(BusinessException exception) {
        HttpStatus status = HttpStatus.CONFLICT; // 409 Conflict
        ErrorResponse response = ErrorResponse.builder()
                .httpStatusCode(status.value())
                .errorMessage(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    // UNPROCESSABLE BUSINESS EXCEPTION
    // Handles valid requests that cannot be completed because a domain rule is not satisfied.
    @ExceptionHandler({
            CartIsEmptyException.class,
            IncompleteProfileException.class,
            ProductInactiveException.class
    })
    public ResponseEntity<ErrorResponse> handleUnprocessableBusinessException(BusinessException exception) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;
        ErrorResponse response = ErrorResponse.builder()
                .httpStatusCode(status.value())
                .errorMessage(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    // BUSINESS EXCEPTION
    // Handles future business exceptions that are not assigned to a more specific handler.
    @ExceptionHandler(BusinessException.class)
    public <T extends BusinessException> ResponseEntity<ErrorResponse> handleBusinessException(T exception) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;
        ErrorResponse response = ErrorResponse.builder()
                .httpStatusCode(status.value())
                .errorMessage(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    // REQUEST BODY VALIDATION EXCEPTION
    // Handles invalid DTO fields reported by @Valid on a request body.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleRequestBodyValidation(MethodArgumentNotValidException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ErrorResponse response = ErrorResponse.builder()
                .httpStatusCode(status.value())
                .errorMessage(message)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    // METHOD VALIDATION EXCEPTION
    // Handles invalid path variables, query parameters, and method-level constraints.
    @ExceptionHandler({
            HandlerMethodValidationException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleMethodValidation(Exception exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST; // 400 Bad Request
        ErrorResponse response = ErrorResponse.builder()
                .httpStatusCode(status.value())
                .errorMessage(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    // MALFORMED REQUEST EXCEPTION
    // Handles unreadable JSON and request parameters that cannot be converted to the required type.
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleMalformedRequest(Exception exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse response = ErrorResponse.builder()
                .httpStatusCode(status.value())
                .errorMessage("Malformed request or invalid parameter type")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    // ILLEGAL ARGUMENT EXCEPTION
    // Handles invalid values rejected by services, entities, and domain value objects.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST; // 400 Bad Rquest
        ErrorResponse response = ErrorResponse.builder()
                .httpStatusCode(status.value())
                .errorMessage(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    // NULL VALUE EXCEPTION
    // Handles required domain values that are explicitly rejected when null.
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse response = ErrorResponse.builder()
                .httpStatusCode(status.value())
                .errorMessage(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    // INTERNAL SERVER EXCEPTION
    // Handles uncaught infrastructure and programming errors without exposing internal details.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        LOGGER.error("Unexpected exception handled", exception);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse response = ErrorResponse.builder()
                .httpStatusCode(status.value())
                .errorMessage("An unexpected error occurred")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
