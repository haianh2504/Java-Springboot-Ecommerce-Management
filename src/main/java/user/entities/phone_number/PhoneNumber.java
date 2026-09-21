package user.entities.phone_number;

public record PhoneNumber(String phoneNumber) {
    public PhoneNumber{
        if(phoneNumber == null)
        {
            throw new NullPointerException("PhoneNumber cannot be null");
        }
        else if(!phoneNumber.matches("^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$"))
        {
            throw new IllegalArgumentException("Invalid PhoneNumber in VietNam");
        }
    }
}
