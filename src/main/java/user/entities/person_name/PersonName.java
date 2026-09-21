package user.entities.person_name;

public record PersonName(String name) {

    public PersonName {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }
        name = name.trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if (!name.matches("^[\\p{L}]+(?:[ '\\-][\\p{L}]+)*$")) {
            throw new IllegalArgumentException("Invalid person name");
        }
    }
}
