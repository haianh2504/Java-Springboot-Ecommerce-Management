package user.entities.email;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EmailConverter implements AttributeConverter<Email, String> {
    @Override
    public String convertToDatabaseColumn(Email email) {
        return email == null ? null : email.email();
    }

    @Override
    public Email convertToEntityAttribute(String email) {
        return email == null ? null : new Email(email);
    }
}
