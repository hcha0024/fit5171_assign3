package rockets.model;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class UserUnitTest {
    private User target;

    @BeforeEach
    public void setUp() {
        target = new User();
    }

    // FIRST NAME

    @DisplayName("should throw exception when pass a empty string to setFirstName function")
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    public void shouldThrowExceptionWhenSetFirstNameToEmpty(String firstName) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> target.setFirstName(firstName));
        assertEquals("firstName cannot be null or empty", exception.getMessage());
    }

    @DisplayName("should throw exception when pass null to setFirstName function")
    @Test
    public void shouldThrowExceptionWhenSetFirstNameToNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> target.setFirstName(null));
        assertEquals("firstName cannot be null or empty", exception.getMessage());
    }

    @DisplayName("should throw exception when pass a name longer than 128 characters to setFirstName function")
    @ParameterizedTest
    @ValueSource(strings = {"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque pen"})
    public void shouldThrowExceptionWhenFirstNameExceeds128Characters(String firstName) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> target.setFirstName(firstName));
        assertEquals("firstName cannot exceed 128 characters", exception.getMessage());
    }

    // LAST NAME

    @DisplayName("should throw exception when pass a empty string to setLastName function")
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    public void shouldThrowExceptionWhenSetLastNameToEmpty(String lastName) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> target.setLastName(lastName));
        assertEquals("lastName cannot be null or empty", exception.getMessage());
    }

    @DisplayName("should throw exception when pass null to setLastName function")
    @Test
    public void shouldThrowExceptionWhenSetLastNameToNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> target.setLastName(null));
        assertEquals("lastName cannot be null or empty", exception.getMessage());
    }

    @DisplayName("should throw exception when pass a last name longer than 64 characters to setLastName function")
    @ParameterizedTest
    @ValueSource(strings = {"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean"})
    public void shouldThrowExceptionWhenLastNameExceeds64Characters(String lastName) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> target.setLastName(lastName));
        assertEquals("lastName cannot exceed 64 characters", exception.getMessage());
    }


    // EMAIL

    @DisplayName("should throw exception when pass a empty email address to setEmail function")
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    public void shouldThrowExceptionWhenSetEmailToEmpty(String email) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> target.setEmail(email));
        assertEquals("email cannot be null or empty", exception.getMessage());
    }

    @DisplayName("should throw exception when pass null to setEmail function")
    @Test
    public void shouldThrowExceptionWhenSetEmailToNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> target.setEmail(null));
        assertEquals("email cannot be null or empty", exception.getMessage());
    }

    @DisplayName("should throw exception when pass an email address longer than 320 characters to setEmail function")
    @ParameterizedTest
    @ValueSource(strings = {"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo@gmail.com"})
    public void shouldThrowExceptionWhenSetEmailToLongerThan320Characters(String email) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> target.setEmail(email));
        assertEquals("email cannot exceed 320 characters", exception.getMessage());
    }

    @DisplayName("should throw exception when pass an email address shorter than 5 characters to setEmail function")
    @ParameterizedTest
    @ValueSource(strings = {"e@.co"})
    public void shouldThrowExceptionWhenSetEmailShorterThan6Characters(String email) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> target.setEmail(email));
        assertEquals("email cannot be shorter than 5 characters", exception.getMessage());
    }

    // PASSWORD

    @DisplayName("should throw exceptions when pass a null password to setPassword function")
    @Test
    public void shouldThrowExceptionWhenSetPasswordToNull() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> target.setPassword(null));
        assertEquals("password cannot be null or empty", exception.getMessage());
    }

    @DisplayName("should throw exception when pass a empty password to setPassword function")
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    public void shouldThrowExceptionWhenSetPasswordToEmpty(String password) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> target.setPassword(password));
        assertEquals("password cannot be null or empty", exception.getMessage());
    }

    @DisplayName("should throw exception when pass a password longer than 128 characters to setPassword function")
    @ParameterizedTest
    @ValueSource(strings = {"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque pen"})
    public void shouldThrowExceptionWhenSetPasswordToLongerThan128Characters(String password) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> target.setPassword(password));
        assertEquals("password cannot exceed 128 characters", exception.getMessage());
    }

    @DisplayName("should throw exception when pass a password shorter than 6 characters to setPassword function")
    @ParameterizedTest
    @ValueSource(strings = {"Loremi"})
    public void shouldThrowExceptionWhenSetPasswordToShorterThan6Characters(String password) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> target.setPassword(password));
        assertEquals("password cannot be shorter than 6 characters", exception.getMessage());
    }

    // OBJECT COMPARISON

    @DisplayName("should return true when two users have the same email")
    @Test
    public void shouldReturnTrueWhenUsersHaveSameEmail() {
        String email = "abc@example.com";
        try {
            target.setEmail(email);
        } catch (Exception e) { }
        User anotherUser = new User();
        try {
            anotherUser.setEmail(email);
        } catch (Exception e) { }
        assertTrue(target.equals(anotherUser));
    }

    @DisplayName("should return false when two users have different emails")
    @Test
    public void shouldReturnFalseWhenUsersHaveDifferentEmails() {
        try {
            target.setEmail("abc@example.com");
        } catch (Exception e) { }
        User anotherUser = new User();
        try {
            anotherUser.setEmail("def@example.com");
        } catch (Exception e) { }
        assertFalse(target.equals(anotherUser));
    }
    @AfterEach
    void tearDown() {
        System.out.println("Tearing down...");
    }

    @AfterAll
    static void freeExternalResources() {
        System.out.println("Freeing external resources...");
    }
}