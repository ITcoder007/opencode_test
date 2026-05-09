import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class HelloTest {

    @Test
    @DisplayName("getGreeting returns Hello, World!")
    void testGetGreeting() {
        assertEquals("Hello, World!", Hello.getGreeting());
    }

    @Test
    @DisplayName("getGreeting returns non-null value")
    void testGetGreetingNotNull() {
        assertNotNull(Hello.getGreeting());
    }

    @Test
    @DisplayName("getGreeting returns non-empty string")
    void testGetGreetingNonEmpty() {
        assertFalse(Hello.getGreeting().isEmpty());
    }

    @Test
    @DisplayName("main runs without exception")
    void testMainRuns() {
        assertDoesNotThrow(() -> Hello.main(new String[]{}));
    }
}
