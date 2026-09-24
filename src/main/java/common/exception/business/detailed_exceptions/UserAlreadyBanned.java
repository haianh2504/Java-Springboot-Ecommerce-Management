package common.exception.business.detailed_exceptions;

import common.exception.business.BusinessException;
import user.entities.User;

public final class UserAlreadyBanned extends BusinessException {
    // should add reason why they got banned later
    public UserAlreadyBanned(User user)
    {
        super(String.format(
                "Cannot perform action on user [%s]: account is already banned", user.getName().name()
        ));
    }
    public UserAlreadyBanned(User user,Throwable cause)
    {
        super(String.format(
                "Cannot perform action on user [%s]: account is already banned", user.getName().name()
        ), cause);
    }
}
