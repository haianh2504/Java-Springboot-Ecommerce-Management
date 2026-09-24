package common.exception.business.detailed_exceptions;

import common.exception.business.BusinessException;
import user.entities.User;

public final class UserAlreadyActive extends BusinessException {
    public UserAlreadyActive(User user)
    {
      super(String.format(
              "User [%s] is already active", user.getName().name()
      ));
    }
    public UserAlreadyActive(User user, Throwable cause)
    {
        super(String.format(
                "User [%s] is already active", user.getName().name()
        ), cause);
    }
}
