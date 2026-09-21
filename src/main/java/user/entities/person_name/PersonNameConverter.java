package user.entities.person_name;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PersonNameConverter implements AttributeConverter<PersonName, String> {
    @Override
    public String convertToDatabaseColumn(PersonName personName) {
        return personName == null ? null : personName.name();
    }

    @Override
    public PersonName convertToEntityAttribute(String personName) {
        return personName == null ? null : new PersonName(personName);
    }
}
