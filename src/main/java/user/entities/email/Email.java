package user.entities.email;

public record Email(String email) {
    public Email{
        if(email == null){
            throw new NullPointerException("Email cannot be null");
        }
        if(email.isBlank())
        {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if(!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
        {
            throw new IllegalArgumentException("Invalid Email");
        }
    }
}
