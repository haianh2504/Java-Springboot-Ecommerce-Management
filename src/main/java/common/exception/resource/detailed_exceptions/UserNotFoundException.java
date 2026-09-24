package common.exception.resource.detailed_exceptions;

import common.exception.resource.ResourceException;
import user.entities.email.Email;

public class UserNotFoundException extends ResourceException {
    // id
    public UserNotFoundException(Long id) {
        super(String.format("User with id %d not found", id));
    }
    public UserNotFoundException(Long id, Throwable cause) {
        super(String.format("User with id %d not found",id), cause);
    }

    // email
    public UserNotFoundException(Email email) {
        super(String.format("User with email %s not found", email.toString()));
    }
    public UserNotFoundException(Email email, Throwable cause) {
        super(String.format("User with email %s not found", email.toString()), cause);
    }
}
